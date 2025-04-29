package com.elyashevich.bank.api.controller;

import com.elyashevich.bank.api.dto.user.UserResponseDto;
import com.elyashevich.bank.api.dto.user.UserSearchRequest;
import com.elyashevich.bank.api.mapper.UserMapper;
import com.elyashevich.bank.entity.UserES;
import com.elyashevich.bank.service.UserElasticsearchService;
import com.elyashevich.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserElasticsearchService userElasticsearchService;
    private final UserMapper userMapper;

    @GetMapping("/search")
    public ResponseEntity<Page<UserES>> searchUsers(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate dateOfBirth,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 10) Pageable pageable) {

        var request = new UserSearchRequest();
        request.setDateOfBirth(dateOfBirth);
        request.setPhone(phone);
        request.setName(name);
        request.setEmail(email);

        var result = userElasticsearchService.searchUsers(request, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll() {
        var users = this.userService.findAll();
        return ResponseEntity.ok(this.userMapper.toDtoList(users));
    }
}
