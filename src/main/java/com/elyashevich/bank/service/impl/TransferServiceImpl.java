package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.domain.entity.Account;
import com.elyashevich.bank.exception.BusinessException;
import com.elyashevich.bank.exception.ResourceNotFoundException;
import com.elyashevich.bank.repository.AccountRepository;
import com.elyashevich.bank.repository.RedisLockRepository;
import com.elyashevich.bank.repository.UserRepository;
import com.elyashevich.bank.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final RedisLockRepository redisLockRepository;

    @Transactional
    public void transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        validateTransferParameters(fromUserId, toUserId, amount);

        var fromUserLockKey = "account_lock:" + fromUserId;
        var toUserLockKey = "account_lock:" + toUserId;

        try {
            var fromUserLockAcquired = redisLockRepository.lock(fromUserLockKey, 10, TimeUnit.SECONDS);
            var toUserLockAcquired = redisLockRepository.lock(toUserLockKey, 10, TimeUnit.SECONDS);

            if (!fromUserLockAcquired || !toUserLockAcquired) {
                throw new BusinessException("Could not acquire locks for transfer operation");
            }

            var message = "User with id: '%s' is locked.";

            var fromAccount = accountRepository.findByUserIdWithPessimisticLock(fromUserId)
                    .orElseThrow(() -> new ResourceNotFoundException(message.formatted(fromUserId)));
            var toAccount = accountRepository.findByUserIdWithPessimisticLock(toUserId)
                    .orElseThrow(() -> new ResourceNotFoundException(message.formatted(toUserId)));

            validateTransferConditions(fromAccount, toAccount, amount);

            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            log.info("Transfer completed: {} RUB from user {} to user {}", amount, fromUserId, toUserId);
        } finally {
            redisLockRepository.unlock(fromUserLockKey);
            redisLockRepository.unlock(toUserLockKey);
        }
    }

    private void validateTransferParameters(Long fromUserId, Long toUserId, BigDecimal amount) {
        if (fromUserId == null || toUserId == null) {
            var message = "User IDs cannot be null";
            log.warn(message);
            throw new BusinessException(message);
        }

        if (fromUserId.equals(toUserId)) {
            var message = "Cannot transfer money to the same account";
            log.warn(message);
            throw new BusinessException(message);
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            var message = "Transfer amount must be positive";
            log.warn(message);
            throw new BusinessException(message);
        }

        var message = "User with id: '%s' was not found";

        if (!userRepository.existsById(fromUserId)) {
            throw new ResourceNotFoundException(message.formatted(fromUserId));
        }

        if (!userRepository.existsById(toUserId)) {
            throw new ResourceNotFoundException(message.formatted(toUserId));
        }
    }

    private void validateTransferConditions(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            var message = "User with is: '%s' with balance: '%s' can not transfer '%s'"
                    .formatted(fromAccount.getUser().getId(), fromAccount.getBalance(), amount);
            log.warn(message);
            throw new BusinessException(message);
        }

        BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
        if (newFromBalance.compareTo(BigDecimal.ZERO) < 0) {
            var message = "Transfer would result in negative balance";
            log.warn(message);
            throw new BusinessException(message);
        }

        // Check if receiver would exceed max balance (207% of initial deposit)
        BigDecimal maxAllowedBalance = toAccount.getBalance().multiply(new BigDecimal("2.07"));
        BigDecimal newToBalance = toAccount.getBalance().add(amount);
        if (newToBalance.compareTo(maxAllowedBalance) > 0) {
            var message = "Receiver would exceed maximum allowed balance";
            log.warn(message);
            throw new BusinessException(message);
        }
    }
}