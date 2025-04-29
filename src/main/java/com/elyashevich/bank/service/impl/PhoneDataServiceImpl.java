package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.exception.ResourceNotFoundException;
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

    @Override
    public void delete(String phone) {
        log.debug("Attempting delete phone with value: '{}'", phone);

        var phoneData = this.phoneDataRepository.findByPhone(phone).orElseThrow(
                () -> {
                    var message = "Phone with value: '%s' was nor found".formatted(phone);
                    log.warn(message);
                    return new ResourceNotFoundException(message);
                }
        );

        phoneData.setUser(null);
        this.phoneDataRepository.delete(phoneData);

        log.info("Phone with value: '{}' deleted", phone);
    }
}
