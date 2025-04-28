package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.entity.Account;
import com.elyashevich.bank.entity.User;
import com.elyashevich.bank.exception.ResourceAlreadyExistsException;
import com.elyashevich.bank.repository.AccountRepository;
import com.elyashevich.bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Account create(User user) {
        log.debug("Attempting create account for user: {}", user);

        if (this.checkIfExists(user)) {
            var message = "Account with user: '%s' already exists".formatted(user);
            log.warn(message);
            throw new ResourceAlreadyExistsException(message);
        }

        var account = this.accountRepository.save(Account.builder()
                .balance(user.getAccount().getBalance())
                .user(user)
                .build());

        log.info("Account for user created: {}", user);
        return account;
    }

    private boolean checkIfExists(User user) {
        log.debug("Check if user already has account: '{}'", user);
        return this.accountRepository.existsByUser(user);
    }
}
