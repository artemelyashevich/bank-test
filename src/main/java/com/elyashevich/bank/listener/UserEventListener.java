package com.elyashevich.bank.listener;

import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.domain.event.EntityEvent;
import com.elyashevich.bank.domain.event.UserAggregate;
import com.elyashevich.bank.service.UserElasticsearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final UserElasticsearchService userElasticsearchService;

    @EventListener
    public void handleUserEvent(EntityEvent<UserAggregate> event) {
        switch (event.getAction()) {
            case CREATE, UPDATE -> userElasticsearchService.index(event.getEntity());
            case DELETE -> userElasticsearchService.delete(event.getEntity().getId());
        }
    }
}
