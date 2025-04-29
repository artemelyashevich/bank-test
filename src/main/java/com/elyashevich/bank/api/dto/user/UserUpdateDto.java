package com.elyashevich.bank.api.dto.user;

import java.util.List;

public record UserUpdateDto(
        List<String> phones,
        List<String> emails
) {
}
