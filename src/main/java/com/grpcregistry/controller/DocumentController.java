package com.grpcregistry.controller;

import com.grpcregistry.dto.request.AddVersionRequest;
import com.grpcregistry.dto.request.CreateDocumentRequest;
import com.grpcregistry.dto.request.StatusTransitionRequest;
import com.grpcregistry.dto.request.VerifyHashRequest;
import com.grpcregistry.dto.response.DocumentResponse;
import com.grpcregistry.dto.response.DocumentVersionResponse;
import com.grpcregistry.dto.response.VerifyHashResponse;
import com.grpcregistry.service.DocumentRegistryService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Controller("/api/v1/documents")
@ExecuteOn(TaskExecutors.BLOCKING)
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRegistryService service;
    private final DocumentRestMapper mapper;

    @Post
    @Status(HttpStatus.CREATED)
    public DocumentResponse create(
            @Body @Valid CreateDocumentRequest request,
            @Nullable @Header("Idempotency-Key") String idempotencyKey
    ) {
        return mapper.toResponse(service.create(request.title(), request.content(), idempotencyKey));
    }

    @Get("/{id}")
    public DocumentResponse getById(UUID id) {
        return mapper.toResponse(service.getById(id));
    }

    @Get
    public Page<DocumentResponse> listAll(Pageable pageable) {
        return mapper.toResponsePage(service.listAll(pageable));
    }

    @Post("/{id}/publish")
    public DocumentResponse publish(UUID id) {
        return mapper.toResponse(service.publish(id));
    }

    @Post("/{id}/archive")
    public DocumentResponse archive(UUID id, @Body @Valid StatusTransitionRequest request) {
        return mapper.toResponse(service.archive(id, request.reason()));
    }

    @Post("/{id}/revoke")
    public DocumentResponse revoke(UUID id, @Body @Valid StatusTransitionRequest request) {
        return mapper.toResponse(service.revoke(id, request.reason()));
    }

    @Post("/{id}/versions")
    @Status(HttpStatus.CREATED)
    public DocumentVersionResponse addVersion(UUID id, @Body @Valid AddVersionRequest request) {
        return mapper.toVersionResponse(
                service.addVersion(id, request.content(), request.changelog(), request.createdBy()));
    }

    @Get("/{id}/versions")
    public Page<DocumentVersionResponse> getVersionHistory(UUID id, Pageable pageable) {
        return mapper.toVersionResponsePage(service.getVersionHistory(id, pageable));
    }

    @Post("/{id}/verify")
    public VerifyHashResponse verifyHash(UUID id, @Body @Valid VerifyHashRequest request) {
        return mapper.toVerifyResponse(service.verifyContentHash(id, request.content()));
    }
}
