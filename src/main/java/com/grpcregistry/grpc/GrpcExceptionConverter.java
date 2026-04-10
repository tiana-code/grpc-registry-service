package com.grpcregistry.grpc;

import com.grpcregistry.model.InvalidStatusTransitionException;
import com.grpcregistry.service.DuplicateContentException;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.persistence.EntityNotFoundException;

public final class GrpcExceptionConverter {

    private GrpcExceptionConverter() {
    }

    public static StatusRuntimeException convert(Throwable exception) {
        if (exception instanceof EntityNotFoundException) {
            return Status.NOT_FOUND
                    .withDescription(exception.getMessage())
                    .asRuntimeException();
        }
        if (exception instanceof DuplicateContentException) {
            return Status.ALREADY_EXISTS
                    .withDescription(exception.getMessage())
                    .asRuntimeException();
        }
        if (exception instanceof InvalidStatusTransitionException) {
            return Status.FAILED_PRECONDITION
                    .withDescription(exception.getMessage())
                    .asRuntimeException();
        }
        if (exception instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT
                    .withDescription(exception.getMessage())
                    .asRuntimeException();
        }

        Metadata metadata = new Metadata();
        metadata.put(
                Metadata.Key.of("error_message", Metadata.ASCII_STRING_MARSHALLER),
                exception.getMessage() != null ? exception.getMessage() : "Unknown error");
        return Status.INTERNAL
                .withDescription(exception.getMessage())
                .asRuntimeException(metadata);
    }
}
