package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.entity.EmailData;
import com.elyashevich.bank.entity.User;
import com.elyashevich.bank.exception.ResourceAlreadyExistsException;
import com.elyashevich.bank.repository.EmailDataRepository;
import com.elyashevich.bank.service.EmailDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDataServiceImpl implements EmailDataService {

    private final EmailDataRepository emailDataRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EmailData create(String email, User user) {
        log.debug("Attempting create email data: {}", email);
        if (this.checkIfExists(email)) {
            var message = "User with email: '%s' already exists".formatted(email);
            log.warn(message);
            throw new ResourceAlreadyExistsException(message);
        }

        var emailData = this.emailDataRepository.save(EmailData.builder()
                .email(email)
                .user(user)
                .build());

        log.info("Email created: {}", email);
        return emailData;
    }

    private boolean checkIfExists(String email) {
        log.debug("Check if email already exists: '{}'", email);
        return this.emailDataRepository.existsByEmail(email);
    }
}
