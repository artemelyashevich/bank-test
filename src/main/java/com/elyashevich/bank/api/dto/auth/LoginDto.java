package com.elyashevich.bank.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Data transfer object for user login",
        requiredProperties = {"email", "password"}
)
public record LoginDto(
        @Schema(
                description = "User's email address",
                example = "user@example.com",
                pattern = "^[A-Za-z0-9+_.-]+@(.+)$",
                minLength = 5,
                maxLength = 255
        )
        @NotNull(message = "Email must be not null")
        @NotBlank(message = "Email must be not blank")
        @Email(message = "Invalid email format")
        String email,

        @Schema(
                description = "User's password",
                example = "securePassword123",
                minLength = 8,
                maxLength = 500,
                format = "password"
        )
        @NotNull(message = "Password must be not null")
        @NotBlank(message = "Password must be not blank")
        @Length(max = 500, min = 8, message = "Password must be in {min} and {max}")
        String password
) {
}