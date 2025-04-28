package com.elyashevich.bank.api.controller;

import com.elyashevich.bank.api.dto.JwtResponse;
import com.elyashevich.bank.api.dto.LoginDto;
import com.elyashevich.bank.api.dto.RegisterDto;
import com.elyashevich.bank.api.mapper.UserMapper;
import com.elyashevich.bank.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(
            @Valid @RequestBody RegisterDto registerDto,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        var user = this.userMapper.toEntity(registerDto);
        var response = this.authService.register(user);
        return ResponseEntity.created(
                uriComponentsBuilder.replacePath("/api/v1/users/{email}")
                        .build(Map.of("email", registerDto.email()))
        ).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginDto loginDto,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        var user = this.userMapper.toEntity(loginDto);
        var response = this.authService.login(user);
        return ResponseEntity.created(
                uriComponentsBuilder.replacePath("/api/v1/users/{email}")
                        .build(Map.of("email", loginDto.email()))
        ).body(response);
    }
}
