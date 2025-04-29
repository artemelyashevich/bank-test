package com.elyashevich.bank.api.controller;

import com.elyashevich.bank.api.dto.auth.JwtResponse;
import com.elyashevich.bank.api.dto.auth.LoginDto;
import com.elyashevich.bank.api.dto.auth.RegisterDto;
import com.elyashevich.bank.api.mapper.UserMapper;
import com.elyashevich.bank.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterDto registerDto) {
        var user = this.userMapper.toEntity(registerDto);
        var response = this.authService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginDto loginDto) {
        var user = this.userMapper.toEntity(loginDto);
        var response = this.authService.login(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
