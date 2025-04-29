package com.elyashevich.bank.service;

import com.elyashevich.bank.domain.entity.User;

public interface JwtService {

    Long extractId(String token);

    String generateToken(User user, long tokenLifeTime);
}
