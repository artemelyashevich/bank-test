package com.elyashevich.bank.repository;

import com.elyashevich.bank.entity.EmailData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailDAtaRepository extends JpaRepository<EmailData, Long> {
}
