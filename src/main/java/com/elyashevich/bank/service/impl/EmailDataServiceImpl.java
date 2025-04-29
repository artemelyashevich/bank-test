package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.repository.EmailDataRepository;
import com.elyashevich.bank.service.EmailDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDataServiceImpl implements EmailDataService {

    private final EmailDataRepository emailDataRepository;

    @Override
    public boolean existsByEmailAndAnotherUser(String email, User user) {
        log.debug("Attempting check if emailData exists by email: {}", email);
        return emailDataRepository.existsByEmailAndAnotherUser(email, user);
    }
}
