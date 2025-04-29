package com.elyashevich.bank.service;

import com.elyashevich.bank.domain.entity.PhoneData;
import com.elyashevich.bank.domain.entity.User;

import java.util.List;

public interface PhoneDataService {

    boolean existsByPhoneAndAnotherUser(String phone, User user);

    void delete(String phone);
}
