package com.elyashevich.bank.service;

import com.elyashevich.bank.entity.PhoneData;
import com.elyashevich.bank.entity.User;

public interface PhoneDataService {

    PhoneData create(String phone, User user);
}
