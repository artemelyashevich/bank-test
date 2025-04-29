package com.elyashevich.bank.service;

import com.elyashevich.bank.api.dto.auth.JwtResponse;
import com.elyashevich.bank.domain.entity.User;

public interface AuthService {

    JwtResponse register(User user);

    JwtResponse login(User user);
}
