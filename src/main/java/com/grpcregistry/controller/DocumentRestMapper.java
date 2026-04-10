package com.grpcregistry.controller;

import com.grpcregistry.dto.response.DocumentResponse;
import com.grpcregistry.dto.response.DocumentVersionResponse;
import com.grpcregistry.dto.response.VerifyHashResponse;
import com.grpcregistry.model.Document;
import com.grpcregistry.model.DocumentVersion;
import io.micronaut.data.model.Page;
import jakarta.inject.Singleton;

@Singleton
public class DocumentRestMapper {

    public DocumentResponse toResponse(Document document) {
        return DocumentResponse.from(document);
    }

    public DocumentVersionResponse toVersionResponse(DocumentVersion version) {
        return DocumentVersionResponse.from(version);
    }

    public Page<DocumentResponse> toResponsePage(Page<Document> documents) {
        return documents.map(DocumentResponse::from);
    }

    public Page<DocumentVersionResponse> toVersionResponsePage(Page<DocumentVersion> versions) {
        return versions.map(DocumentVersionResponse::from);
    }

    public VerifyHashResponse toVerifyResponse(boolean valid) {
        return new VerifyHashResponse(valid);
    }
}
