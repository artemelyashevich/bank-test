package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.entity.User;
import com.elyashevich.bank.exception.ResourceNotFoundException;
import com.elyashevich.bank.repository.UserRepository;
import com.elyashevich.bank.service.AccountService;
import com.elyashevich.bank.service.EmailDataService;
import com.elyashevich.bank.service.PhoneDataService;
import com.elyashevich.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final EmailDataService emailDataService;
    private final PhoneDataService phoneDataService;
    private final AccountService accountService;

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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = this.findByEmail(username);
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString())
                .password(user.getPassword())
                .build();
    }
}
