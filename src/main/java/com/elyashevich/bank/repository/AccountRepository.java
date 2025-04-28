package com.elyashevich.bank.repository;

import com.elyashevich.bank.entity.Account;
import com.elyashevich.bank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUser(User user);
}
