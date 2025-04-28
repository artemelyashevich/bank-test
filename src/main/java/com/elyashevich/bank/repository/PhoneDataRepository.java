package com.elyashevich.bank.repository;

import com.elyashevich.bank.entity.PhoneData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {

    boolean existsByPhone(String phone);
}
