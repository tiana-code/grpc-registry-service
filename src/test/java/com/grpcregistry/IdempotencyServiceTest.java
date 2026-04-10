package com.grpcregistry;

import com.grpcregistry.idempotency.IdempotencyKey;
import com.grpcregistry.idempotency.IdempotencyService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@MicronautTest
class IdempotencyServiceTest {

    @Inject
    IdempotencyService idempotencyService;

    @Test
    void isDuplicate_returnsFalseForNewKey() {
        assertThat(idempotencyService.isDuplicate("fresh-key-" + System.nanoTime())).isFalse();
    }

    @Test
    void storeResponse_thenIsDuplicate_returnsTrue() {
        String key = "idem-key-" + System.nanoTime();
        idempotencyService.storeResponse(key, 201, "{\"id\":\"123\"}");

        assertThat(idempotencyService.isDuplicate(key)).isTrue();
    }

    @Test
    void findExisting_returnsStoredRecord() {
        String key = "idem-key-" + System.nanoTime();
        idempotencyService.storeResponse(key, 201, "{\"id\":\"456\"}");

        Optional<IdempotencyKey> existing = idempotencyService.findExisting(key);
        assertThat(existing).isPresent();
        assertThat(existing.get().getResponseBody()).isEqualTo("{\"id\":\"456\"}");
        assertThat(existing.get().getHttpStatus()).isEqualTo(201);
    }

    @Test
    void findExisting_returnsEmptyForUnknownKey() {
        assertThat(idempotencyService.findExisting("unknown-key")).isEmpty();
    }

    @Test
    void storeResponse_withVeryShortTtl_expiredKeyNotConsideredDuplicate() throws InterruptedException {
        String key = "expiring-key-" + System.nanoTime();
        idempotencyService.storeResponse(key, 201, "data", Duration.ofMillis(1));

        Thread.sleep(10);

        assertThat(idempotencyService.isDuplicate(key)).isFalse();
        assertThat(idempotencyService.findExisting(key)).isEmpty();
    }

    @Test
    void purgeExpired_removesExpiredRecords() throws InterruptedException {
        String expiredKey = "expired-" + System.nanoTime();
        idempotencyService.storeResponse(expiredKey, 201, "remove", Duration.ofMillis(1));

        Thread.sleep(10);

        idempotencyService.purgeExpired();
        assertThat(idempotencyService.isDuplicate(expiredKey)).isFalse();
    }
}
