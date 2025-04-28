package com.elyashevich.bank.service;

import com.elyashevich.bank.entity.Account;
import com.elyashevich.bank.entity.User;

public interface AccountService {

    Account create(User user);
}
