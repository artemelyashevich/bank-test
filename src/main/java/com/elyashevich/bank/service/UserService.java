package com.elyashevich.bank.service;

import com.elyashevich.bank.domain.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    User findById(Long id);

    User findByEmail(String email);

    User create(User user);

    List<User> findAll();

    User update(Long userId, User user);
}
