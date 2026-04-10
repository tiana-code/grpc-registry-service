package com.grpcregistry.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface DocumentEvent permits
        DocumentEvent.Created,
        DocumentEvent.Published,
        DocumentEvent.Archived,
        DocumentEvent.Revoked,
        DocumentEvent.VersionAdded {

    UUID documentId();

    Instant occurredAt();

    record Created(UUID documentId, String title, String contentHash, Instant occurredAt) implements DocumentEvent {
    }

    record Published(UUID documentId, Instant occurredAt) implements DocumentEvent {
    }

    record Archived(UUID documentId, String reason, Instant occurredAt) implements DocumentEvent {
    }

    record Revoked(UUID documentId, String reason, Instant occurredAt) implements DocumentEvent {
    }

    record VersionAdded(UUID documentId, UUID versionId, String contentHash, String createdBy,
                        Instant occurredAt) implements DocumentEvent {
    }
}
