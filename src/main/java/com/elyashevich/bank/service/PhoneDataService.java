package com.elyashevich.bank.service;

import com.elyashevich.bank.domain.entity.User;

public interface PhoneDataService {

    boolean existsByPhoneAndAnotherUser(String phone, User user);
}
