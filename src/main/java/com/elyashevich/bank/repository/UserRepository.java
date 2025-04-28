package com.elyashevich.bank.repository;

import com.elyashevich.bank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
                SELECT u
                FROM User u
                WHERE u.id = (
                    SELECT e.user.id
                    FROM EmailData e
                    WHERE e.email = :email
                    )
            """)
    Optional<User> findByEmail(@Param("email") String email);
}
