package com.grpcregistry.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "documents",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_documents_content_hash", columnNames = "content_hash")
        },
        indexes = {
                @Index(name = "idx_documents_status", columnList = "status")
        }
)
@Getter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Document() {
    }

    public Document(String title, String contentHash, DocumentStatus status) {
        this.title = title;
        this.contentHash = contentHash;
        this.status = status;
    }

    public void transitionTo(DocumentStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition from " + this.status + " to " + target);
        }
        this.status = target;
    }

    public void updateContentHash(String contentHash) {
        this.contentHash = contentHash;
    }
}
