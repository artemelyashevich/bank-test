package com.elyashevich.bank.service;

import com.elyashevich.bank.domain.entity.Account;
import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.exception.BusinessException;
import com.elyashevich.bank.exception.ResourceNotFoundException;
import com.elyashevich.bank.repository.AccountRepository;
import com.elyashevich.bank.repository.RedisLockRepository;
import com.elyashevich.bank.repository.UserRepository;
import com.elyashevich.bank.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisLockRepository redisLockRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private Account fromAccount;
    private Account toAccount;
    private final Long fromUserId = 1L;
    private final Long toUserId = 2L;
    private final BigDecimal amount = BigDecimal.valueOf(100);

    @BeforeEach
    void setUp() {
        User fromUser = new User();
        fromUser.setId(fromUserId);
        fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setUser(fromUser);
        fromAccount.setBalance(BigDecimal.valueOf(500));

        User toUser = new User();
        toUser.setId(toUserId);
        toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setUser(toUser);
        toAccount.setBalance(BigDecimal.valueOf(200));
    }

    @Test
    @Transactional
    void transferShouldCompleteSuccessfully() {
        // Mock dependencies
        when(redisLockRepository.lock(anyString(), anyLong(), any())).thenReturn(true);
        when(accountRepository.findByUserIdWithPessimisticLock(fromUserId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserIdWithPessimisticLock(toUserId)).thenReturn(Optional.of(toAccount));
        when(userRepository.existsById(fromUserId)).thenReturn(true);
        when(userRepository.existsById(toUserId)).thenReturn(true);

        // Execute
        transferService.transfer(fromUserId, toUserId, amount);

        // Verify
        assertAll(
            () -> assertEquals(BigDecimal.valueOf(400), fromAccount.getBalance()),
            () -> assertEquals(BigDecimal.valueOf(300), toAccount.getBalance()),
            () -> verify(accountRepository, times(2)).save(any()),
            () -> verify(redisLockRepository).unlock("account_lock:" + fromUserId),
            () -> verify(redisLockRepository).unlock("account_lock:" + toUserId)
        );
    }

    @Test
    void transferShouldThrowWhenToUserNotFound() {
        when(userRepository.existsById(fromUserId)).thenReturn(true);
        when(userRepository.existsById(toUserId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
            () -> transferService.transfer(fromUserId, toUserId, amount));
    }

    @Test
    void transferShouldThrowWhenSameUser() {
        assertThrows(BusinessException.class,
            () -> transferService.transfer(fromUserId, fromUserId, amount));
    }

    @ParameterizedTest
    @MethodSource("invalidAmounts")
    void transferShouldThrowWhenInvalidAmount(BigDecimal amount) {
        assertThrows(BusinessException.class,
            () -> transferService.transfer(fromUserId, toUserId, amount));
    }

    private static Stream<BigDecimal> invalidAmounts() {
        return Stream.of(
            BigDecimal.ZERO,
            BigDecimal.valueOf(-100),
            null
        );
    }

    @Test
    void transferShouldThrowWhenLockNotAcquired() {
        when(redisLockRepository.lock(anyString(), anyLong(), any())).thenReturn(false);
        when(userRepository.existsById(fromUserId)).thenReturn(true);
        when(userRepository.existsById(toUserId)).thenReturn(true);

        assertThrows(BusinessException.class,
            () -> transferService.transfer(fromUserId, toUserId, amount));
    }

    @Test
    void transferShouldThrowWhenInsufficientFunds() {
        fromAccount.setBalance(BigDecimal.valueOf(50));
        when(redisLockRepository.lock(anyString(), anyLong(), any())).thenReturn(true);
        when(accountRepository.findByUserIdWithPessimisticLock(fromUserId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserIdWithPessimisticLock(toUserId)).thenReturn(Optional.of(toAccount));
        when(userRepository.existsById(fromUserId)).thenReturn(true);
        when(userRepository.existsById(toUserId)).thenReturn(true);

        assertThrows(BusinessException.class,
            () -> transferService.transfer(fromUserId, toUserId, amount));
    }

    @Test
    void transferShouldThrowWhenExceedsMaxBalance() {
        toAccount.setBalance(BigDecimal.valueOf(1000));
        when(redisLockRepository.lock(anyString(), anyLong(), any())).thenReturn(true);
        when(accountRepository.findByUserIdWithPessimisticLock(fromUserId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserIdWithPessimisticLock(toUserId)).thenReturn(Optional.of(toAccount));
        when(userRepository.existsById(fromUserId)).thenReturn(true);
        when(userRepository.existsById(toUserId)).thenReturn(true);

        assertThrows(BusinessException.class,
            () -> transferService.transfer(fromUserId, toUserId, BigDecimal.valueOf(1000)));
    }

    @Test
    void transferShouldUnlockEvenOnFailure() {
        when(redisLockRepository.lock(anyString(), anyLong(), any())).thenReturn(true);
        when(userRepository.existsById(fromUserId)).thenReturn(true);
        when(userRepository.existsById(toUserId)).thenReturn(true);
        when(accountRepository.findByUserIdWithPessimisticLock(fromUserId)).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class,
            () -> transferService.transfer(fromUserId, toUserId, amount));

        verify(redisLockRepository).unlock("account_lock:" + fromUserId);
        verify(redisLockRepository).unlock("account_lock:" + toUserId);
    }

    @Test
    void validateTransferConditionsShouldThrowForInsufficientFunds() {
        fromAccount.setBalance(BigDecimal.valueOf(50));
        assertThrows(BusinessException.class,
            () -> validateTransferConditions(fromAccount, toAccount, amount));
    }

    @Test
    void validateTransferConditionsShouldThrowForMaxBalanceExceeded() {
        toAccount.setBalance(BigDecimal.valueOf(1000));
        assertThrows(BusinessException.class,
            () -> validateTransferConditions(fromAccount, toAccount, BigDecimal.valueOf(1000)));
    }

    private void validateTransferConditions(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            var message = "User with is: '%s' with balance: '%s' can not transfer '%s'"
                    .formatted(fromAccount.getUser().getId(), fromAccount.getBalance(), amount);
            throw new BusinessException(message);
        }

        BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
        if (newFromBalance.compareTo(BigDecimal.ZERO) < 0) {
            var message = "Transfer would result in negative balance";
            throw new BusinessException(message);
        }

        // Check if receiver would exceed max balance (207% of initial deposit)
        BigDecimal maxAllowedBalance = toAccount.getBalance().multiply(new BigDecimal("2.07"));
        BigDecimal newToBalance = toAccount.getBalance().add(amount);
        if (newToBalance.compareTo(maxAllowedBalance) > 0) {
            var message = "Receiver would exceed maximum allowed balance";
            throw new BusinessException(message);
        }
    }
}