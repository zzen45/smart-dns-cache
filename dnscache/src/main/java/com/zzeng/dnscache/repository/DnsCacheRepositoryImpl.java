package com.zzeng.dnscache.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Objects;

@Repository
public class DnsCacheRepositoryImpl implements DnsCacheRepository {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    public DnsCacheRepositoryImpl(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<String> get(String domain) {
        return redisTemplate.opsForValue()
                .get(domain)
                .filter(Objects::nonNull);
    }

    @Override
    public Mono<Boolean> set(String domain, String value, long ttlSeconds) {
        return redisTemplate.opsForValue()
                .set(domain, value, Duration.ofSeconds(ttlSeconds))
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> delete(String domain) {
        return redisTemplate.delete(domain).map(count -> count > 0);
    }

    @Override
    public Flux<String> scanKeys() {
        return redisTemplate.scan();
    }
}
