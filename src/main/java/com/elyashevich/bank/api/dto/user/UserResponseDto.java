package com.elyashevich.bank.api.dto.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UserResponseDto(
        Long id,
        String name,
        List<String> emails,
        List<String> phones,
        BigDecimal balance,
        LocalDate dateOfBirth
) implements Serializable {
}
