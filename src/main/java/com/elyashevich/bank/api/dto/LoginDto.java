package com.elyashevich.bank.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record LoginDto(
        @NotNull(message = "Email must be not null")
        @NotBlank(message = "Email must be not blank")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Password must be not null")
        @NotBlank(message = "Password must be not blank")
        @Length(max = 500, min = 8, message = "Password must be in {min} and {max}")
        String password
        ) {
}
