package com.elyashevich.bank.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterDto(

        @NotNull(message = "Email must be not null")
        @NotBlank(message = "Email must be not blank")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message= "Name must be not null")
        @NotBlank(message = "Name must be not blank")
        @Length(max = 500, min=1, message = "Name must be in {min} and {max}")
        String name,

        LocalDate dateOfBirth,

        @NotNull(message= "Password must be not null")
        @NotBlank(message = "Password must be not blank")
        @Length(max = 500, min=8, message = "Password must be in {min} and {max}")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Password must contain at least one digit, one lowercase, one uppercase letter, and one special character"
        )
        String password,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^7[0-9]{10}$", message = "Phone must be 11 digits starting with 7")
        String phone,

        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Initial balance must be positive")
        @Digits(integer = 16, fraction = 2, message = "Balance must have up to 16 integer and 2 fraction digits")
        BigDecimal initialBalance
) {
}
