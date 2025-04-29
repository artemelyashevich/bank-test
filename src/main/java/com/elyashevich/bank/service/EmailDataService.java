package com.elyashevich.bank.service;

import com.elyashevich.bank.domain.entity.User;

public interface EmailDataService {

    boolean existsByEmailAndAnotherUser(String email, User user);

    void delete(String email);
}
