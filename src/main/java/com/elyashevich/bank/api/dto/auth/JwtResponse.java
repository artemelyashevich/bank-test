package com.elyashevich.bank.api.dto.auth;

public record JwtResponse(
        String accessToken,
        String refreshToken
) {
}
