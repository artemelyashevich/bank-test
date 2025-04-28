package com.elyashevich.bank.service;

import com.elyashevich.bank.api.dto.JwtResponse;
import com.elyashevich.bank.entity.User;

public interface AuthService {

    JwtResponse register(User user);

    JwtResponse login(User user);
}
