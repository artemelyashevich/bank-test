package com.elyashevich.bank.repository;

import com.elyashevich.bank.domain.entity.PhoneData;
import com.elyashevich.bank.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {

    @Query("""
        SELECT (count(p) > 0)
        FROM PhoneData p
        WHERE p.phone = :phone and p.user != :user
        """)
    boolean existsByPhoneAndAnotherUser(String phone, User user);
}
