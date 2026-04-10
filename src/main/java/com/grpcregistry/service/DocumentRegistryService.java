package com.grpcregistry.service;

import com.grpcregistry.model.Document;
import com.grpcregistry.model.DocumentVersion;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

import java.util.UUID;

public interface DocumentRegistryService {

    Document create(String title, String content, String idempotencyKey);

    Document getById(UUID id);

    Page<Document> listAll(Pageable pageable);

    Document publish(UUID id);

    Document archive(UUID id, String reason);

    Document revoke(UUID id, String reason);

    DocumentVersion addVersion(UUID documentId, String content, String changelog, String createdBy);

    Page<DocumentVersion> getVersionHistory(UUID documentId, Pageable pageable);

    boolean verifyContentHash(UUID documentId, String content);
}
