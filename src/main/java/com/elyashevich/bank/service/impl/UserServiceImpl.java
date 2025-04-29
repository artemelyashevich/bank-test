package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.entity.User;
import com.elyashevich.bank.event.EntityEvent;
import com.elyashevich.bank.event.EventAction;
import com.elyashevich.bank.exception.ResourceNotFoundException;
import com.elyashevich.bank.repository.UserRepository;
import com.elyashevich.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

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
    public User update(User user) {
        return null;
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
}
