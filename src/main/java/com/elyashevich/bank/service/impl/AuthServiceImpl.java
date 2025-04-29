package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.api.dto.auth.JwtResponse;
import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.service.AuthService;
import com.elyashevich.bank.service.JwtService;
import com.elyashevich.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.jwt.lifetime.access:1800000}")
    private Long accessTokenLifetime;

    @Value("${application.security.jwt.lifetime.refresh:864000000}")
    private Long refreshTokenLifetime;

    @Override
    public JwtResponse register(User user) {
        log.debug("Attempting register new user: {}", user);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var newUser = this.userService.create(user);

        var response = this.generateJwt(newUser);

        log.info("New user has been registered: '{}'", newUser);
        return response;
    }

    @Override
    public JwtResponse login(User candidate) {
        log.debug("Attempting authenticate user: '{}'", candidate);

        var user = this.userService.findByEmail(candidate.getEmails().getFirst().getEmail());

        var response = this.generateJwt(user);

        log.info("User: '{}' has been logged in", user);
        return response;
    }

    private JwtResponse generateJwt(User user) {
        var accessToken = this.jwtService.generateToken(user, this.accessTokenLifetime);
        var refreshToken = this.jwtService.generateToken(user, this.refreshTokenLifetime);
        return new JwtResponse(accessToken, refreshToken);
    }
}
