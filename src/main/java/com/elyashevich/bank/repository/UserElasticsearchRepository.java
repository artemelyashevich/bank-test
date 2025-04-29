package com.elyashevich.bank.repository;

import com.elyashevich.bank.entity.UserES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserElasticsearchRepository extends ElasticsearchRepository<UserES, Long> {
}
