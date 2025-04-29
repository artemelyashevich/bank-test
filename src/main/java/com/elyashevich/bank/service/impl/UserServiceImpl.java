package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.domain.entity.EmailData;
import com.elyashevich.bank.domain.entity.PhoneData;
import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.domain.event.EntityEvent;
import com.elyashevich.bank.domain.event.EventAction;
import com.elyashevich.bank.exception.ResourceAlreadyExistsException;
import com.elyashevich.bank.exception.ResourceNotFoundException;
import com.elyashevich.bank.repository.UserRepository;
import com.elyashevich.bank.service.EmailDataService;
import com.elyashevich.bank.service.PhoneDataService;
import com.elyashevich.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PhoneDataService phoneDataService;
    private final EmailDataService emailDataService;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public User findById(Long id) {
        log.debug("Attempting find user by id: '{}'", id);

        var user = this.userRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("User with id: '%s' was not found", id);
                    log.warn(message);
                    return new ResourceNotFoundException(message);
                });

        log.info("Found by id: {}, user: {}", id, user);
        return user;
    }

    @Override
    public User findByEmail(String email) {
        log.debug("Attempting find user by email: {}", email);

        var user = this.userRepository.findByEmail(email).orElseThrow(
                () -> {
                    var message = "User with email: '%s' was not found".formatted(email);
                    log.warn(message);
                    return new ResourceNotFoundException(message);
                }
        );

        log.info("User with email: '{}' found: '{}' ", email, user);
        return user;
    }

    @Override
    @Transactional
    public User create(User user) {
        log.debug("Attempting create new user: {}", user);

        user.getEmails().forEach(email -> email.setUser(user));
        user.getPhones().forEach(phone -> phone.setUser(user));
        user.getAccount().setUser(user);

        var newUser = this.userRepository.save(user);

        publishEvent(EventAction.CREATE, newUser);
        log.info("New user created: '{}'", newUser);
        return newUser;
    }

    @Override
    public List<User> findAll() {
        log.debug("Attempting find All users");

        var users = this.userRepository.findAll();

        log.info("Found users: {}", users.size());
        return users;
    }

    @Override
    @Transactional
    public User update(Long userId, User candidate) {
        log.debug("Attempting to update user with id: {}", userId);

        User user = this.findById(userId);

        this.validateAndUpdateEmails(user, candidate);
        this.validateAndUpdatePhones(user, candidate);

        var updatedUser = userRepository.save(user);

        this.publishEvent(EventAction.UPDATE, updatedUser);

        log.info("Successfully updated user with id: {}", userId);
        return updatedUser;
    }

    @Override
    @Transactional
    public User deleteEmailsAndPhones(Long userId, User candidate) {
        log.debug("Attempting delete user emails and phones: {}", candidate);

        var user = this.findById(userId);

        this.validateAndDeleteEmails(user, candidate);
        this.validateAndDeletePhones(user, candidate);

        var updatedUser = userRepository.save(user);

        this.publishEvent(EventAction.UPDATE, updatedUser);

        log.info("Successfully updated user emails and phones with id: {}", userId);
        return updatedUser;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = this.findByEmail(username);
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString())
                .password(user.getPassword())
                .build();
    }

    private void publishEvent(EventAction action, User user) {
        var event = new EntityEvent<>(action, user);
        redisTemplate.convertAndSend("user-events", event);
    }

    private void validateAndUpdateEmails(User user, User candidate) {
        var existingEmails = user.getEmails().stream()
                .map(EmailData::getEmail)
                .collect(Collectors.toSet());

        candidate.getEmails().forEach(newEmail -> {
            var email = newEmail.getEmail();
            if (!existingEmails.contains(email)) {
                if (emailDataService.existsByEmailAndAnotherUser(email, user)) {
                    throw new ResourceAlreadyExistsException(
                            String.format("Email already exists: '%s'", email));
                }
                newEmail.setUser(user);
                user.getEmails().add(newEmail);
            }
        });
    }

    private void validateAndUpdatePhones(User user, User candidate) {
        var existingPhones = user.getPhones().stream()
                .map(PhoneData::getPhone)
                .collect(Collectors.toSet());

        candidate.getPhones().forEach(newPhone -> {
            var phone = newPhone.getPhone();
            if (!existingPhones.contains(phone)) {
                if (phoneDataService.existsByPhoneAndAnotherUser(phone, user)) {
                    throw new ResourceAlreadyExistsException(
                            String.format("Phone already exists: '%s'", phone));
                }
                newPhone.setUser(user);
                user.getPhones().add(newPhone);
            }
        });
    }

    private void validateAndDeletePhones(User user, User candidate) {
        var existingPhones = user.getPhones().stream()
                .map(PhoneData::getPhone)
                .collect(Collectors.toSet());

        candidate.getPhones().forEach(p -> {
            var phone = p.getPhone();
            if (!existingPhones.contains(phone)) {
                throw new ResourceNotFoundException("User didn't have phone: '%s'".formatted(phone));
            }
            user.setPhones(user.getPhones().stream()
                    .filter(ph -> !ph.getPhone().equals(phone))
                    .toList()
            );
        });
    }

    private void validateAndDeleteEmails(User user, User candidate) {
        var existingEmails = user.getEmails().stream()
                .map(EmailData::getEmail)
                .collect(Collectors.toSet());

        candidate.getEmails().forEach(e -> {
            var email = e.getEmail();
            if (!existingEmails.contains(email)) {
                throw new ResourceNotFoundException("User didn't have phone: '%s'".formatted(email));
            }
            user.setEmails(user.getEmails().stream()
                    .filter(ph -> !ph.getEmail().equals(email))
                    .toList()
            );
        });
    }
}
