package com.elyashevich.bank.api.controller;

import com.elyashevich.bank.api.dto.user.UserResponseDto;
import com.elyashevich.bank.api.mapper.UserMapper;
import com.elyashevich.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll() {
        var users = this.userService.findAll();
        return ResponseEntity.ok(this.userMapper.toDtoList(users));
    }
}
