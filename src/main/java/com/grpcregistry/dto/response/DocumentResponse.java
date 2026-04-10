package com.grpcregistry.dto.response;

import com.grpcregistry.model.Document;
import com.grpcregistry.model.DocumentStatus;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.UUID;

@Serdeable
public record DocumentResponse(
        UUID id,
        String title,
        String contentHash,
        DocumentStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getContentHash(),
                document.getStatus(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
