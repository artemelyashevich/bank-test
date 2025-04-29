package com.elyashevich.bank.service;

import com.elyashevich.bank.api.dto.user.UserSearchRequest;
import com.elyashevich.bank.domain.es.UserES;
import com.elyashevich.bank.domain.event.UserAggregate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserElasticsearchService {

    Page<UserES> searchUsers(UserSearchRequest request, Pageable pageable);

    void index(UserAggregate user);

    void delete(Long id);
}
