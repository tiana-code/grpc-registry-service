package com.grpcregistry.dto.response;

import com.grpcregistry.model.DocumentVersion;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.UUID;

@Serdeable
public record DocumentVersionResponse(
        UUID versionId,
        UUID documentId,
        String contentHash,
        String changelog,
        String createdBy,
        Instant createdAt
) {
    public static DocumentVersionResponse from(DocumentVersion version) {
        return new DocumentVersionResponse(
                version.getVersionId(),
                version.getDocumentId(),
                version.getContentHash(),
                version.getChangelog(),
                version.getCreatedBy(),
                version.getCreatedAt()
        );
    }
}
