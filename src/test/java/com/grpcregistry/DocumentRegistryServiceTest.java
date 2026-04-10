package com.grpcregistry;

import com.grpcregistry.model.Document;
import com.grpcregistry.model.DocumentStatus;
import com.grpcregistry.model.DocumentVersion;
import com.grpcregistry.model.InvalidStatusTransitionException;
import com.grpcregistry.service.DocumentRegistryService;
import com.grpcregistry.service.DuplicateContentException;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@MicronautTest
class DocumentRegistryServiceTest {

    @Inject
    DocumentRegistryService service;

    @Test
    void createDocument_persistsWithDraftStatusAndInitialVersion() {
        Document doc = service.create("Test Doc", "Hello World", null);

        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(doc.getContentHash()).isNotBlank();
        assertThat(doc.getVersion()).isNotNull();
    }

    @Test
    void createDocument_withIdempotencyKey_replaysResultOnDuplicate() {
        Document first = service.create("Doc A", "Content A", "key-001");
        Document replayed = service.create("Doc A Again", "Content B", "key-001");

        assertThat(replayed.getId()).isEqualTo(first.getId());
    }

    @Test
    void publish_transitionsDraftToPublished() {
        Document doc = service.create("To Publish", "body", null);
        Document published = service.publish(doc.getId());

        assertThat(published.getStatus()).isEqualTo(DocumentStatus.PUBLISHED);
    }

    @Test
    void publish_fromArchivedStatus_throwsInvalidTransition() {
        Document doc = service.create("To Archive", "body", null);
        service.publish(doc.getId());
        service.archive(doc.getId(), "outdated");

        assertThatThrownBy(() -> service.publish(doc.getId()))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void publish_fromDraftOnly_rejectsPublishedDocument() {
        Document doc = service.create("Doc", "content", null);
        service.publish(doc.getId());

        assertThatThrownBy(() -> service.publish(doc.getId()))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void archive_requiresPublishedStatus() {
        Document doc = service.create("Doc", "content", null);

        assertThatThrownBy(() -> service.archive(doc.getId(), "reason"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void archive_transitionsPublishedToArchived() {
        Document doc = service.create("Doc", "content", null);
        service.publish(doc.getId());
        Document archived = service.archive(doc.getId(), "no longer needed");

        assertThat(archived.getStatus()).isEqualTo(DocumentStatus.ARCHIVED);
    }

    @Test
    void revoke_transitionsPublishedToRevoked() {
        Document doc = service.create("Doc", "content", null);
        service.publish(doc.getId());
        Document revoked = service.revoke(doc.getId(), "policy violation");

        assertThat(revoked.getStatus()).isEqualTo(DocumentStatus.REVOKED);
    }

    @Test
    void revoke_fromDraft_throwsInvalidTransition() {
        Document doc = service.create("Doc", "content", null);

        assertThatThrownBy(() -> service.revoke(doc.getId(), "reason"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void addVersion_recordsVersionHistoryAndUpdatesHash() {
        Document doc = service.create("Versioned Doc", "v1 content", null);
        DocumentVersion v2 = service.addVersion(doc.getId(), "v2 content", "Second version", "alice");

        assertThat(v2.getVersionId()).isNotNull();
        assertThat(v2.getCreatedBy()).isEqualTo("alice");

        Page<DocumentVersion> history = service.getVersionHistory(doc.getId(), Pageable.from(0, 10));
        assertThat(history.getTotalSize()).isEqualTo(2);
    }

    @Test
    void addVersion_withDuplicateContent_throwsDuplicateContentException() {
        Document doc = service.create("Doc", "same content", null);

        assertThatThrownBy(() -> service.addVersion(doc.getId(), "same content", "retry", "bob"))
                .isInstanceOf(DuplicateContentException.class);
    }

    @Test
    void addVersion_toArchivedDocument_throwsInvalidTransition() {
        Document doc = service.create("Doc", "content", null);
        service.publish(doc.getId());
        service.archive(doc.getId(), "reason");

        assertThatThrownBy(() -> service.addVersion(doc.getId(), "new content", "update", "carol"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void verifyContentHash_returnsTrueForMatchingContent() {
        Document doc = service.create("Doc", "exact content", null);

        assertThat(service.verifyContentHash(doc.getId(), "exact content")).isTrue();
        assertThat(service.verifyContentHash(doc.getId(), "different content")).isFalse();
    }

    @Test
    void listAll_returnsPaginatedResults() {
        service.create("Doc 1", "content 1", null);
        service.create("Doc 2", "content 2", null);

        Page<Document> page = service.listAll(Pageable.from(0, 10));
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }
}
