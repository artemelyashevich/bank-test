package com.elyashevich.bank.service;

import com.elyashevich.bank.entity.User;

public interface UserService {

    User findByEmail(String email);

    User create(User user);
}
