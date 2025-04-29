package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.entity.Account;
import com.elyashevich.bank.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceUpdateService {

    private final AccountRepository accountRepository;

    @Transactional
    @Scheduled(fixedRate = 30000)
    public void increaseBalances() {
        var accounts = accountRepository.findAll();

        for (Account account : accounts) {
            var currentBalance = account.getBalance();
            var initialBalance = account.getBalance();
            var maxBalance = initialBalance.multiply(BigDecimal.valueOf(2.07));
            var newBalance = currentBalance.multiply(BigDecimal.valueOf(1.10));

            if (newBalance.compareTo(maxBalance) > 0) {
                account.setBalance(maxBalance);
            } else {
                account.setBalance(newBalance);
            }
        }

        accountRepository.saveAll(accounts);
    }
}