package com.grpcregistry.repository;

import com.grpcregistry.model.DocumentVersion;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

import java.util.UUID;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    @Query(value = "SELECT dv FROM DocumentVersion dv WHERE dv.document.id = :documentId",
            countQuery = "SELECT COUNT(dv) FROM DocumentVersion dv WHERE dv.document.id = :documentId")
    Page<DocumentVersion> findByDocumentId(UUID documentId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(dv) > 0 THEN true ELSE false END FROM DocumentVersion dv WHERE dv.document.id = :documentId AND dv.contentHash = :contentHash")
    boolean existsByDocumentIdAndContentHash(UUID documentId, String contentHash);
}
