package com.elyashevich.bank.repository;

import com.elyashevich.bank.domain.entity.EmailData;
import com.elyashevich.bank.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmailDataRepository extends JpaRepository<EmailData, Long> {

    @Query("""
        SELECT (count(e) > 0)
        FROM EmailData e
        WHERE e.email = :email and e.user != :user
        """)
    boolean existsByEmailAndAnotherUser(String email, User user);
}
