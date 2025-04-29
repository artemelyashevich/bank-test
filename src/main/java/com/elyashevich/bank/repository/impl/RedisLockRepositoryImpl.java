package com.elyashevich.bank.repository.impl;

import com.elyashevich.bank.repository.RedisLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisLockRepositoryImpl implements RedisLockRepository {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean lock(String key, long timeout, TimeUnit unit) {
        var acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", timeout, unit);

        return acquired != null && acquired;
    }

    @Override
    public void unlock(String key) {
        redisTemplate.delete(key);
    }
}