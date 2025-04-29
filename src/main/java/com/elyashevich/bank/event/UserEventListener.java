package com.elyashevich.bank.event;

import com.elyashevich.bank.entity.User;
import com.elyashevich.bank.service.UserElasticsearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final UserElasticsearchService userElasticsearchService;

    @EventListener
    public void handleUserEvent(EntityEvent<User> event) {
        switch (event.getAction()) {
            case CREATE, UPDATE -> userElasticsearchService.index(event.getEntity());
            case DELETE -> userElasticsearchService.delete(event.getEntity().getId());
        }
    }
}
