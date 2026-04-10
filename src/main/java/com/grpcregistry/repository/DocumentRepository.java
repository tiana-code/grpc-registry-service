package com.grpcregistry.repository;

import com.grpcregistry.model.Document;
import com.grpcregistry.model.DocumentStatus;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByContentHash(String contentHash);

    boolean existsByContentHash(String contentHash);

    Page<Document> findAllByStatus(DocumentStatus status, Pageable pageable);
}
