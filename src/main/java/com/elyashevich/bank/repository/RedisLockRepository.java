package com.elyashevich.bank.repository;

import java.util.concurrent.TimeUnit;

public interface RedisLockRepository {
    boolean lock(String key, long timeout, TimeUnit unit);

    void unlock(String key);
}