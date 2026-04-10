package com.grpcregistry.idempotency;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {

    Optional<IdempotencyKey> findByKey(String key);

    @Query("DELETE FROM IdempotencyKey ik WHERE ik.expiresAt < :now")
    void deleteExpired(Instant now);
}
