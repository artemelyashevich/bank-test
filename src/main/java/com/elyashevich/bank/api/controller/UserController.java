package com.elyashevich.bank.api.controller;

import com.elyashevich.bank.api.dto.user.UserDto;
import com.elyashevich.bank.api.dto.user.UserResponseDto;
import com.elyashevich.bank.api.dto.user.UserSearchRequest;
import com.elyashevich.bank.api.mapper.UserMapper;
import com.elyashevich.bank.domain.es.UserES;
import com.elyashevich.bank.service.UserElasticsearchService;
import com.elyashevich.bank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponseDto> findById(@PathVariable("id") Long id) {
        var user = this.userService.findById(id);
        return ResponseEntity.ok(this.userMapper.toDto(user));
    }

    @GetMapping("/current")
    public ResponseEntity<UserResponseDto> findCurrent() {
        var id = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        var user = this.userService.findById(id);
        return ResponseEntity.ok(this.userMapper.toDto(user));
    }

    @PutMapping
    public ResponseEntity<UserResponseDto> update(
            @Valid @RequestBody UserDto userUpdateDto,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        var userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        var user = this.userService.update(userId, this.userMapper.toEntity(userUpdateDto));
        return ResponseEntity.created(
                        uriComponentsBuilder.replacePath("/api/v1/users/{id}")
                                .build(Map.of("id", user.getId()))
                )
                .body(this.userMapper.toDto(user));
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePhoneAndEmail(@Valid @RequestBody UserDto userDto) {
        var userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        this.userService.deleteEmailsAndPhones(userId, this.userMapper.toEntity(userDto));
        return ResponseEntity.noContent().build();
    }
}
