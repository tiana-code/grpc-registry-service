package com.grpcregistry.grpc;

import com.grpcregistry.model.Document;
import com.grpcregistry.model.DocumentVersion;
import com.grpcregistry.proto.*;
import com.grpcregistry.service.DocumentRegistryService;
import io.grpc.stub.StreamObserver;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class DocumentGrpcService extends DocumentRegistryServiceGrpc.DocumentRegistryServiceImplBase {

    private final DocumentRegistryService service;
    private final GrpcMapper mapper;

    @Override
    public void createDocument(CreateDocumentRequest request, StreamObserver<DocumentResponse> responseObserver) {
        log.debug("gRPC createDocument: title={}", request.getTitle());
        try {
            String idempotencyKey = request.getIdempotencyKey().isBlank() ? null : request.getIdempotencyKey();
            Document document = service.create(request.getTitle(), request.getContent(), idempotencyKey);
            responseObserver.onNext(mapper.toProto(document));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcExceptionConverter.convert(e));
        }
    }

    @Override
    public void getDocument(GetDocumentRequest request, StreamObserver<DocumentResponse> responseObserver) {
        log.debug("gRPC getDocument: id={}", request.getDocumentId());
        try {
            Document document = service.getById(UUID.fromString(request.getDocumentId()));
            responseObserver.onNext(mapper.toProto(document));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcExceptionConverter.convert(e));
        }
    }

    @Override
    public void listDocuments(ListDocumentsRequest request, StreamObserver<ListDocumentsResponse> responseObserver) {
        log.debug("gRPC listDocuments: page={} size={}", request.getPage(), request.getSize());
        try {
            int page = Math.max(request.getPage(), 0);
            int size = request.getSize() > 0 ? request.getSize() : 20;

            Page<Document> result = service.listAll(Pageable.from(page, size));
            ListDocumentsResponse.Builder builder = ListDocumentsResponse.newBuilder()
                    .setTotalElements((int) result.getTotalSize())
                    .setTotalPages(result.getTotalPages())
                    .setCurrentPage(result.getPageNumber());

            result.getContent().forEach(doc -> builder.addDocuments(mapper.toProto(doc)));

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcExceptionConverter.convert(e));
        }
    }

    @Override
    public void publishDocument(PublishDocumentRequest request, StreamObserver<DocumentResponse> responseObserver) {
        log.debug("gRPC publishDocument: id={}", request.getDocumentId());
        try {
            Document document = service.publish(UUID.fromString(request.getDocumentId()));
            responseObserver.onNext(mapper.toProto(document));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcExceptionConverter.convert(e));
        }
    }

    @Override
    public void archiveDocument(ArchiveDocumentRequest request, StreamObserver<DocumentResponse> responseObserver) {
        log.debug("gRPC archiveDocument: id={}", request.getDocumentId());
        try {
            Document document = service.archive(UUID.fromString(request.getDocumentId()), request.getReason());
            responseObserver.onNext(mapper.toProto(document));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcExceptionConverter.convert(e));
        }
    }

    @Override
    public void revokeDocument(RevokeDocumentRequest request, StreamObserver<DocumentResponse> responseObserver) {
        log.debug("gRPC revokeDocument: id={}", request.getDocumentId());
        try {
            Document document = service.revoke(UUID.fromString(request.getDocumentId()), request.getReason());
            responseObserver.onNext(mapper.toProto(document));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcExceptionConverter.convert(e));
        }
    }

    @Override
    public void addVersion(AddVersionRequest request, StreamObserver<DocumentVersionResponse> responseObserver) {
        log.debug("gRPC addVersion: documentId={}", request.getDocumentId());
        try {
            DocumentVersion version = service.addVersion(
                    UUID.fromString(request.getDocumentId()),
                    request.getContent(),
                    request.getChangelog(),
                    request.getCreatedBy());
            responseObserver.onNext(mapper.toProto(version));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcExceptionConverter.convert(e));
        }
    }

    @Override
    public void getVersionHistory(GetVersionHistoryRequest request, StreamObserver<GetVersionHistoryResponse> responseObserver) {
        log.debug("gRPC getVersionHistory: documentId={}", request.getDocumentId());
        try {
            int page = Math.max(request.getPage(), 0);
            int size = request.getSize() > 0 ? request.getSize() : 20;

            Page<DocumentVersion> result = service.getVersionHistory(
                    UUID.fromString(request.getDocumentId()), Pageable.from(page, size));

            GetVersionHistoryResponse.Builder builder = GetVersionHistoryResponse.newBuilder()
                    .setTotalElements((int) result.getTotalSize())
                    .setTotalPages(result.getTotalPages())
                    .setCurrentPage(result.getPageNumber());

            result.getContent().forEach(v -> builder.addVersions(mapper.toProto(v)));

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcExceptionConverter.convert(e));
        }
    }

    @Override
    public void verifyContentHash(VerifyContentHashRequest request, StreamObserver<VerifyContentHashResponse> responseObserver) {
        log.debug("gRPC verifyContentHash: documentId={}", request.getDocumentId());
        try {
            boolean valid = service.verifyContentHash(
                    UUID.fromString(request.getDocumentId()), request.getContent());
            responseObserver.onNext(VerifyContentHashResponse.newBuilder().setValid(valid).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcExceptionConverter.convert(e));
        }
    }
}
