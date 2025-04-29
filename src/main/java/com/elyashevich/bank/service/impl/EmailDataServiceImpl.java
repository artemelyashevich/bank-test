package com.elyashevich.bank.service.impl;

import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.exception.ResourceNotFoundException;
import com.elyashevich.bank.repository.EmailDataRepository;
import com.elyashevich.bank.service.EmailDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public void delete(String email) {
        log.debug("Attempting delete email data with email: {}", email);

        var emailData = this.emailDataRepository.findByEmail(email).orElseThrow(
                () -> {
                    var message = "Email with value: '%s' was not found.".formatted(email);
                    log.warn(message);
                    return new ResourceNotFoundException(message);
                }
        );
        emailData.setUser(null);

        this.emailDataRepository.delete(emailData);
        log.info("Email with value: '{}' deleted", email);
    }
}
