package com.grpcregistry.controller;

import com.grpcregistry.dto.request.AddVersionRequest;
import com.grpcregistry.dto.request.CreateDocumentRequest;
import com.grpcregistry.dto.request.StatusTransitionRequest;
import com.grpcregistry.dto.request.VerifyHashRequest;
import com.grpcregistry.dto.response.DocumentResponse;
import com.grpcregistry.dto.response.DocumentVersionResponse;
import com.grpcregistry.service.DocumentRegistryService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Controller("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRegistryService service;

    @Post
    @Status(HttpStatus.CREATED)
    public DocumentResponse create(
            @Body @Valid CreateDocumentRequest request,
            @Nullable @Header("Idempotency-Key") String idempotencyKey
    ) {
        return DocumentResponse.from(service.create(request.title(), request.content(), idempotencyKey));
    }

    @Get("/{id}")
    public DocumentResponse getById(UUID id) {
        return DocumentResponse.from(service.getById(id));
    }

    @Get
    public Page<DocumentResponse> listAll(Pageable pageable) {
        return service.listAll(pageable).map(DocumentResponse::from);
    }

    @Post("/{id}/publish")
    public DocumentResponse publish(UUID id) {
        return DocumentResponse.from(service.publish(id));
    }

    @Post("/{id}/archive")
    public DocumentResponse archive(UUID id, @Body @Valid StatusTransitionRequest request) {
        return DocumentResponse.from(service.archive(id, request.reason()));
    }

    @Post("/{id}/revoke")
    public DocumentResponse revoke(UUID id, @Body @Valid StatusTransitionRequest request) {
        return DocumentResponse.from(service.revoke(id, request.reason()));
    }

    @Post("/{id}/versions")
    @Status(HttpStatus.CREATED)
    public DocumentVersionResponse addVersion(UUID id, @Body @Valid AddVersionRequest request) {
        return DocumentVersionResponse.from(
                service.addVersion(id, request.content(), request.changelog(), request.createdBy()));
    }

    @Get("/{id}/versions")
    public Page<DocumentVersionResponse> getVersionHistory(UUID id, Pageable pageable) {
        return service.getVersionHistory(id, pageable).map(DocumentVersionResponse::from);
    }

    @Post("/{id}/verify")
    public Map<String, Boolean> verifyHash(UUID id, @Body @Valid VerifyHashRequest request) {
        boolean valid = service.verifyContentHash(id, request.content());
        return Map.of("valid", valid);
    }
}
