package com.grpcregistry.config;

import com.grpcregistry.model.InvalidStatusTransitionException;
import com.grpcregistry.service.DuplicateContentException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityNotFoundException;

import java.util.Map;

public class GlobalExceptionHandler {

    @Singleton
    public static class EntityNotFoundHandler
            implements ExceptionHandler<EntityNotFoundException, HttpResponse<?>> {
        @Override
        public HttpResponse<?> handle(HttpRequest request, EntityNotFoundException exception) {
            return HttpResponse.notFound(Map.of(
                    "status", HttpStatus.NOT_FOUND.getCode(),
                    "detail", exception.getMessage()));
        }
    }

    @Singleton
    public static class DuplicateContentHandler
            implements ExceptionHandler<DuplicateContentException, HttpResponse<?>> {
        @Override
        public HttpResponse<?> handle(HttpRequest request, DuplicateContentException exception) {
            return HttpResponse.status(HttpStatus.CONFLICT).body(Map.of(
                    "status", HttpStatus.CONFLICT.getCode(),
                    "detail", exception.getMessage()));
        }
    }

    @Singleton
    public static class InvalidStatusTransitionHandler
            implements ExceptionHandler<InvalidStatusTransitionException, HttpResponse<?>> {
        @Override
        public HttpResponse<?> handle(HttpRequest request, InvalidStatusTransitionException exception) {
            return HttpResponse.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                    "status", HttpStatus.UNPROCESSABLE_ENTITY.getCode(),
                    "detail", exception.getMessage()));
        }
    }
}
