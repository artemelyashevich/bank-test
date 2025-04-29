package com.elyashevich.bank.service;

import com.elyashevich.bank.api.dto.user.UserSearchRequest;
import com.elyashevich.bank.entity.User;
import com.elyashevich.bank.entity.UserES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserElasticsearchService {

    Page<UserES> searchUsers(UserSearchRequest request, Pageable pageable);

    void index(User user);

    void delete(Long id);
}
