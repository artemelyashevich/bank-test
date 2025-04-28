package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.entity.PhoneData;
import com.elyashevich.bank.entity.User;
import com.elyashevich.bank.exception.ResourceAlreadyExistsException;
import com.elyashevich.bank.repository.PhoneDataRepository;
import com.elyashevich.bank.service.PhoneDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneDataServiceImpl implements PhoneDataService {

    private final PhoneDataRepository phoneDataRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PhoneData create(String phone, User user) {
        log.debug("Attempting create new phone: '{}'", phone);
        if (checkIfExists(phone)) {
            var message = "Phone: '%s' already exists".formatted(phone);
            log.warn(message);
            throw new ResourceAlreadyExistsException(message);
        }
        var phoneData = this.phoneDataRepository.save(PhoneData.builder()
                .phone(phone)
                .user(user)
                .build());

        log.info("Phone data has been created");
        return phoneData;
    }

    private boolean checkIfExists(String phone) {
        log.debug("Check if phone already exists: '{}'", phone);
        return this.phoneDataRepository.existsByPhone(phone);
    }
}
