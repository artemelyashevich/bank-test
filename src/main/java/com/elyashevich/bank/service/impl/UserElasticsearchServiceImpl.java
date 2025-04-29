package com.elyashevich.bank.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.elyashevich.bank.api.dto.user.UserSearchRequest;
import com.elyashevich.bank.domain.entity.EmailData;
import com.elyashevich.bank.domain.entity.PhoneData;
import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.domain.es.UserES;
import com.elyashevich.bank.exception.BusinessException;
import com.elyashevich.bank.repository.UserElasticsearchRepository;
import com.elyashevich.bank.service.UserElasticsearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserElasticsearchServiceImpl implements UserElasticsearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final UserElasticsearchRepository userElasticsearchRepository;

    @Override
    public Page<UserES> searchUsers(UserSearchRequest request, Pageable pageable) {
        try {
            var queryBuilder = new Query.Builder();
            var boolQueryBuilder = new BoolQuery.Builder();

            if (request.getDateOfBirth() != null) {
                boolQueryBuilder.must(q -> q
                        .term(r -> r
                                .field("dateOfBirth")
                                .value(JsonData.of(request.getDateOfBirth().toString()).toString())
                        )
                );
            }

            if (StringUtils.hasText(request.getPhone())) {
                boolQueryBuilder.must(q -> q
                        .term(t -> t
                                .field("phones")
                                .value(request.getPhone())
                        )
                );
            }

            if (StringUtils.hasText(request.getName())) {
                boolQueryBuilder.must(q -> q
                        .prefix(p -> p
                                .field("name")
                                .value(request.getName())
                        )
                );
            }

            if (StringUtils.hasText(request.getEmail())) {
                boolQueryBuilder.must(q -> q
                        .term(t -> t
                                .field("emails")
                                .value(request.getEmail())
                        )
                );
            }

            queryBuilder.bool(boolQueryBuilder.build());

            var response = elasticsearchClient.search(s -> s
                            .index("users")
                            .query(queryBuilder.build())
                            .from((int) pageable.getOffset())
                            .size(pageable.getPageSize())
                            .trackTotalHits(t -> t.enabled(true)),
                    UserES.class
            );

            var users = response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

            long totalHits = response.hits().total().value();

            return new PageImpl<>(users, pageable, totalHits);
        } catch (IOException e) {
            throw new BusinessException("Search failed", e);
        }
    }

    @Override
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

    @Override
    public void delete(Long id) {
        this.userElasticsearchRepository.deleteById(id);
    }
}
