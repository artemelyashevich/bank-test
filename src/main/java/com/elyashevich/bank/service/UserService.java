package com.elyashevich.bank.service;

import com.elyashevich.bank.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User findByEmail(String email);

    User create(User user);
}
