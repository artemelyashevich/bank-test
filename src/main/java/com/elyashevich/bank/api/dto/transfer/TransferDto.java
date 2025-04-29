package com.elyashevich.bank.api.dto.transfer;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Transfer request data")
public record TransferDto(
        @Schema(
                description = "Recipient user ID",
                example = "123",
                minimum = "1"
        )
        @NotNull(message = "Recipient ID must be specified")
        @Positive(message = "Recipient ID must be positive")
        Long toUserId,

        @Schema(
                description = "Transfer amount",
                example = "1000.50",
                minimum = "0.01",
                maximum = "1000000.00"
        )
        @NotNull(message = "Amount must be specified")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        @DecimalMax(value = "1000000.00", message = "Maximum transfer amount is 1,000,000.00")
        @Digits(integer = 10, fraction = 2, message = "Amount must have up to 10 integer and 2 fraction digits")
        BigDecimal amount
) {
}