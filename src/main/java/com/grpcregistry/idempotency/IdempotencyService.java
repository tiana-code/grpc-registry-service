package com.grpcregistry.idempotency;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor
public class IdempotencyService {

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final IdempotencyKeyRepository repository;

    public Optional<IdempotencyKey> findExisting(String key) {
        return repository.findByKey(key)
                .filter(ik -> ik.getExpiresAt().isAfter(Instant.now()));
    }

    @Transactional
    public void storeResponse(String key, int httpStatus, String responseBody) {
        storeResponse(key, httpStatus, responseBody, DEFAULT_TTL);
    }

    @Transactional
    public void storeResponse(String key, int httpStatus, String responseBody, Duration ttl) {
        IdempotencyKey record = new IdempotencyKey(key, httpStatus, responseBody, Instant.now().plus(ttl));
        repository.save(record);
    }

    public boolean isDuplicate(String key) {
        return findExisting(key).isPresent();
    }

    @Transactional
    public void purgeExpired() {
        repository.deleteExpired(Instant.now());
    }
}
