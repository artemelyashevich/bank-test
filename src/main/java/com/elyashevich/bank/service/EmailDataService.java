package com.elyashevich.bank.service;

import com.elyashevich.bank.entity.EmailData;
import com.elyashevich.bank.entity.User;

public interface EmailDataService {

    EmailData create(String email, User user);
}
