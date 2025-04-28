package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.entity.User;
import com.elyashevich.bank.exception.ResourceNotFoundException;
import com.elyashevich.bank.repository.UserRepository;
import com.elyashevich.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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

        var newUser = this.userRepository.save(user);

        log.info("New user created: '{}'", newUser);
        return newUser;
    }
}
