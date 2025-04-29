package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.repository.PhoneDataRepository;
import com.elyashevich.bank.service.PhoneDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneDataServiceImpl implements PhoneDataService {

    private final PhoneDataRepository phoneDataRepository;

    @Override
    public boolean existsByPhoneAndAnotherUser(String phone, User user) {
        log.debug("Attempting check if exists phoneData by phone: '{}' and user: {}", phone, user);
        return phoneDataRepository.existsByPhoneAndAnotherUser(phone, user);
    }
}
