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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {

    private final UserService userService;
    private final UserElasticsearchService userElasticsearchService;
    private final UserMapper userMapper;

    @Operation(
            summary = "Search users",
            description = "Search users with filtering options",
            parameters = {
                    @Parameter(name = "dateOfBirth", description = "Date of birth (dd.MM.yyyy)", example = "15.01.1990"),
                    @Parameter(name = "phone", description = "Phone number to search", example = "79123456789"),
                    @Parameter(name = "name", description = "Name to search (partial match)", example = "John"),
                    @Parameter(name = "email", description = "Email to search (partial match)", example = "user@example.com"),
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Items per page", example = "10"),
                    @Parameter(name = "sort", description = "Sorting criteria (field,direction)", example = "name,asc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users found",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<UserES>> searchUsers(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate dateOfBirth,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        var request = new UserSearchRequest();
        request.setDateOfBirth(dateOfBirth);
        request.setPhone(phone);
        request.setName(name);
        request.setEmail(email);

        var result = userElasticsearchService.searchUsers(request, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieve a list of all users",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of users",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll() {
        var users = this.userService.findAll();
        return ResponseEntity.ok(this.userMapper.toDtoList(users));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a specific user by their ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponseDto> findById(
            @Parameter(description = "ID of the user to retrieve", example = "1")
            @PathVariable("id") Long id) {
        var user = this.userService.findById(id);
        return ResponseEntity.ok(this.userMapper.toDto(user));
    }

    @Operation(
            summary = "Get current user",
            description = "Retrieve information about the currently authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current user details",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/current")
    public ResponseEntity<UserResponseDto> findCurrent() {
        var id = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        var user = this.userService.findById(id);
        return ResponseEntity.ok(this.userMapper.toDto(user));
    }

    @Operation(
            summary = "Update user",
            description = "Update the currently authenticated user's information",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User updated successfully",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "409", description = "Conflict (e.g., email/phone already exists)")
            }
    )
    @PutMapping
    public ResponseEntity<UserResponseDto> update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User data to update",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.class)))
            @Valid @RequestBody UserDto userUpdateDto,
            UriComponentsBuilder uriComponentsBuilder) {

        var userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        var user = this.userService.update(userId, this.userMapper.toEntity(userUpdateDto));
        return ResponseEntity.created(
                        uriComponentsBuilder.replacePath("/api/v1/users/{id}")
                                .build(Map.of("id", user.getId())))
                .body(this.userMapper.toDto(user));
    }

    @Operation(
            summary = "Delete user contacts",
            description = "Remove specified emails and phones from the current user",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Contacts deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "412", description = "Precondition failed (e.g., trying to remove all contacts)")
            }
    )
    @DeleteMapping
    public ResponseEntity<Void> deletePhoneAndEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Contacts to remove",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.class)))
            @Valid @RequestBody UserDto userDto) {

        var userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        this.userService.deleteEmailsAndPhones(userId, this.userMapper.toEntity(userDto));
        return ResponseEntity.noContent().build();
    }
}