package com.elyashevich.bank.api.dto;

public record JwtResponse(
        String accessToken,
        String refreshToken
) {
}
