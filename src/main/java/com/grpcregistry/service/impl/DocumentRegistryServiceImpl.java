package com.grpcregistry.service.impl;

import com.grpcregistry.event.DocumentEvent;
import com.grpcregistry.event.DocumentEventPublisher;
import com.grpcregistry.idempotency.IdempotencyService;
import com.grpcregistry.model.Document;
import com.grpcregistry.model.DocumentStatus;
import com.grpcregistry.model.DocumentVersion;
import com.grpcregistry.model.InvalidStatusTransitionException;
import com.grpcregistry.repository.DocumentRepository;
import com.grpcregistry.repository.DocumentVersionRepository;
import com.grpcregistry.service.DocumentRegistryService;
import com.grpcregistry.service.DuplicateContentException;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor
public class DocumentRegistryServiceImpl implements DocumentRegistryService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ContentHashService contentHashService;
    private final IdempotencyService idempotencyService;
    private final DocumentEventPublisher eventPublisher;

    @Override
    @Transactional
    public Document create(String title, String content, String idempotencyKey) {
        if (idempotencyKey != null) {
            Optional<Document> replayed = idempotencyService.findExisting(idempotencyKey)
                    .map(ik -> getById(UUID.fromString(ik.getResponseBody())));
            if (replayed.isPresent()) {
                return replayed.get();
            }
        }

        String hash = contentHashService.computeHash(content);
        contentHashService.findDuplicateDocumentId(hash).ifPresent(existingId -> {
            throw new DuplicateContentException("Document with identical content already exists: " + existingId);
        });

        Document document = new Document(title, hash, DocumentStatus.DRAFT);
        Document saved = documentRepository.save(document);

        DocumentVersion initialVersion = new DocumentVersion(saved, hash, "Initial version", "system");
        documentVersionRepository.save(initialVersion);

        eventPublisher.publish(new DocumentEvent.Created(saved.getId(), title, hash, Instant.now()));

        if (idempotencyKey != null) {
            idempotencyService.storeResponse(idempotencyKey, 201, saved.getId().toString());
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Document getById(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Document> listAll(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Document publish(UUID id) {
        Document document = getById(id);
        document.transitionTo(DocumentStatus.PUBLISHED);
        Document saved = documentRepository.save(document);
        eventPublisher.publish(new DocumentEvent.Published(id, Instant.now()));
        return saved;
    }

    @Override
    @Transactional
    public Document archive(UUID id, String reason) {
        Document document = getById(id);
        document.transitionTo(DocumentStatus.ARCHIVED);
        Document saved = documentRepository.save(document);
        eventPublisher.publish(new DocumentEvent.Archived(id, reason, Instant.now()));
        return saved;
    }

    @Override
    @Transactional
    public Document revoke(UUID id, String reason) {
        Document document = getById(id);
        document.transitionTo(DocumentStatus.REVOKED);
        Document saved = documentRepository.save(document);
        eventPublisher.publish(new DocumentEvent.Revoked(id, reason, Instant.now()));
        return saved;
    }

    @Override
    @Transactional
    public DocumentVersion addVersion(UUID documentId, String content, String changelog, String createdBy) {
        Document document = getById(documentId);
        if (document.getStatus() == DocumentStatus.ARCHIVED || document.getStatus() == DocumentStatus.REVOKED) {
            throw new InvalidStatusTransitionException(
                    "Cannot add version to document in status: " + document.getStatus());
        }

        String hash = contentHashService.computeHash(content);
        if (documentVersionRepository.existsByDocumentIdAndContentHash(documentId, hash)) {
            throw new DuplicateContentException("Version with identical content already exists for document: " + documentId);
        }

        document.updateContentHash(hash);

        DocumentVersion version = new DocumentVersion(document, hash, changelog, createdBy);
        DocumentVersion saved = documentVersionRepository.save(version);

        eventPublisher.publish(new DocumentEvent.VersionAdded(
                documentId, saved.getVersionId(), hash, createdBy, Instant.now()));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentVersion> getVersionHistory(UUID documentId, Pageable pageable) {
        if (!documentRepository.existsById(documentId)) {
            throw new EntityNotFoundException("Document not found: " + documentId);
        }
        return documentVersionRepository.findByDocumentId(documentId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyContentHash(UUID documentId, String content) {
        Document document = getById(documentId);
        return contentHashService.verifyHash(content, document.getContentHash());
    }
}
