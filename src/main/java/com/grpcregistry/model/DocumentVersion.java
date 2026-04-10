package com.grpcregistry.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "document_versions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_doc_versions_document_hash", columnNames = {"document_id", "content_hash"})
        },
        indexes = {
                @Index(name = "idx_doc_versions_document_id", columnList = "document_id")
        }
)
@Getter
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "version_id", updatable = false, nullable = false)
    private UUID versionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_doc_versions_document"))
    private Document document;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "changelog")
    private String changelog;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected DocumentVersion() {
    }

    public DocumentVersion(Document document, String contentHash, String changelog, String createdBy) {
        this.document = document;
        this.contentHash = contentHash;
        this.changelog = changelog;
        this.createdBy = createdBy;
    }

    public UUID getDocumentId() {
        return document != null ? document.getId() : null;
    }
}
