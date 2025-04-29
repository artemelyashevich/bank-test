package com.elyashevich.bank.es;

import com.elyashevich.bank.entity.EmailData;
import com.elyashevich.bank.entity.PhoneData;
import com.elyashevich.bank.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserElasticsearchService {

    private final UserElasticsearchRepository userElasticsearchRepository;

    public void index(User user) {
        var userEs = UserES.builder()
                .id(user.getId())
                .name(user.getName())
                .emails(user.getEmails().stream()
                        .map(EmailData::getEmail)
                        .toList()
                )
                .phones(user.getPhones().stream()
                        .map(PhoneData::getPhone)
                        .toList()
                )
                .balance(user.getAccount().getBalance())
                .dateOfBirth(user.getDateOfBirth())
                .build();
        this.userElasticsearchRepository.save(userEs);
    }

    public void delete(Long id) {
        this.userElasticsearchRepository.deleteById(id);
    }
}
