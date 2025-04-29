package com.elyashevich.bank.api.dto.auth;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Data transfer object for user registration",
        requiredProperties = {"email", "name", "password", "phone", "balance"}
)
public record RegisterDto(
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
                description = "User's full name",
                example = "John Doe",
                minLength = 1,
                maxLength = 500
        )
        @NotNull(message = "Name must be not null")
        @NotBlank(message = "Name must be not blank")
        @Length(max = 500, min = 1, message = "Name must be in {min} and {max}")
        String name,

        @Schema(
                description = "User's date of birth",
                example = "1990-01-15",
                format = "date"
        )
        LocalDate dateOfBirth,

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
        String password,

        @Schema(
                description = "User's phone number (11 digits starting with 7)",
                example = "79123456789",
                pattern = "^7[0-9]{10}$",
                minLength = 11,
                maxLength = 11
        )
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^7[0-9]{10}$", message = "Phone must be 11 digits starting with 7")
        String phone,

        @Schema(
                description = "Initial account balance",
                example = "1000.00",
                minimum = "0.0",
                exclusiveMinimum = true,
                pattern = "^\\d{1,16}(\\.\\d{1,2})?$"
        )
        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Initial balance must be positive")
        @Digits(integer = 16, fraction = 2, message = "Balance must have up to 16 integer and 2 fraction digits")
        BigDecimal balance
) {
}