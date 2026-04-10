package com.grpcregistry.grpc;

import com.google.protobuf.Timestamp;
import com.grpcregistry.model.Document;
import com.grpcregistry.model.DocumentVersion;
import com.grpcregistry.proto.DocumentResponse;
import com.grpcregistry.proto.DocumentStatus;
import com.grpcregistry.proto.DocumentVersionResponse;
import jakarta.inject.Singleton;

import java.time.Instant;

@Singleton
public class GrpcMapper {

    public DocumentResponse toProto(Document document) {
        DocumentResponse.Builder builder = DocumentResponse.newBuilder()
                .setId(document.getId().toString())
                .setTitle(document.getTitle())
                .setContentHash(document.getContentHash())
                .setStatus(toProtoStatus(document.getStatus()));

        if (document.getCreatedAt() != null) {
            builder.setCreatedAt(toTimestamp(document.getCreatedAt()));
        }
        if (document.getUpdatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(document.getUpdatedAt()));
        }

        return builder.build();
    }

    public DocumentVersionResponse toProto(DocumentVersion version) {
        DocumentVersionResponse.Builder builder = DocumentVersionResponse.newBuilder()
                .setVersionId(version.getVersionId().toString())
                .setDocumentId(version.getDocumentId().toString())
                .setContentHash(version.getContentHash())
                .setCreatedBy(version.getCreatedBy());

        if (version.getChangelog() != null) {
            builder.setChangelog(version.getChangelog());
        }
        if (version.getCreatedAt() != null) {
            builder.setCreatedAt(toTimestamp(version.getCreatedAt()));
        }

        return builder.build();
    }

    public DocumentStatus toProtoStatus(com.grpcregistry.model.DocumentStatus status) {
        return switch (status) {
            case DRAFT -> DocumentStatus.DOCUMENT_STATUS_DRAFT;
            case PUBLISHED -> DocumentStatus.DOCUMENT_STATUS_PUBLISHED;
            case ARCHIVED -> DocumentStatus.DOCUMENT_STATUS_ARCHIVED;
            case REVOKED -> DocumentStatus.DOCUMENT_STATUS_REVOKED;
        };
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
