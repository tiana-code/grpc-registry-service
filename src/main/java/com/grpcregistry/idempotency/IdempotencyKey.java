package com.grpcregistry.idempotency;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_keys",
        indexes = {
                @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
                @Index(name = "idx_idempotency_expires_at", columnList = "expires_at")
        }
)
@Getter
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String key;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected IdempotencyKey() {
    }

    public IdempotencyKey(String key, int httpStatus, String responseBody, Instant expiresAt) {
        this.key = key;
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
        this.expiresAt = expiresAt;
    }
}
