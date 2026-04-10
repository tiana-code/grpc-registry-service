package com.grpcregistry.event;

import io.micronaut.transaction.annotation.TransactionalEventListener;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DocumentEventListener {

    @TransactionalEventListener
    public void onCreated(DocumentEvent.Created event) {
        log.info("AUDIT | document.created documentId={} title={} contentHash={}",
                event.documentId(), event.title(), event.contentHash());
    }

    @TransactionalEventListener
    public void onPublished(DocumentEvent.Published event) {
        log.info("AUDIT | document.published documentId={}", event.documentId());
    }

    @TransactionalEventListener
    public void onArchived(DocumentEvent.Archived event) {
        log.info("AUDIT | document.archived documentId={} reason={}", event.documentId(), event.reason());
    }

    @TransactionalEventListener
    public void onRevoked(DocumentEvent.Revoked event) {
        log.info("AUDIT | document.revoked documentId={} reason={}", event.documentId(), event.reason());
    }

    @TransactionalEventListener
    public void onVersionAdded(DocumentEvent.VersionAdded event) {
        log.info("AUDIT | document.version_added documentId={} versionId={} createdBy={}",
                event.documentId(), event.versionId(), event.createdBy());
    }
}
