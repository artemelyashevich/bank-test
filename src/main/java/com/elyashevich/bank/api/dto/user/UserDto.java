package com.elyashevich.bank.api.dto.user;

import com.elyashevich.bank.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@Schema(
        description = "DTO for updating user contacts (phones and emails)",
        requiredProperties = {"phones", "emails"}
)
public record UserDto(
        @ArraySchema(
                schema = @Schema(
                        description = "List of phone numbers to update",
                        example = "[\"79123456789\", \"79234567890\"]",
                        minLength = 11,
                        maxLength = 11,
                        pattern = "^7[0-9]{10}$"
                ),
                minItems = 1,
                uniqueItems = true
        )
        @NotEmpty(message = "At least one phone number is required")
        List<@Pattern(regexp = "^7[0-9]{10}$", message = "Phone must be 11 digits starting with 7")
        @NotBlank(message = "Phone number cannot be blank") String> phones,

        @ArraySchema(
                schema = @Schema(
                        description = "List of email addresses to update",
                        example = "[\"user@example.com\", \"alternate@example.com\"]",
                        minLength = 5,
                        maxLength = 255,
                        pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                ),
                minItems = 1,
                uniqueItems = true
        )
        @NotEmpty(message = "At least one email is required")
        List<@Email(message = "Invalid email format")
        @NotBlank(message = "Email cannot be blank") String> emails
) {
    public UserDto {
        if (phones != null && phones.stream().anyMatch(phone -> phone == null || phone.trim().isEmpty())) {
            throw new BusinessException("Phone list cannot contain null or blank values");
        }
        if (emails != null && emails.stream().anyMatch(email -> email == null || email.trim().isEmpty())) {
            throw new BusinessException("Email list cannot contain null or blank values");
        }
    }
}