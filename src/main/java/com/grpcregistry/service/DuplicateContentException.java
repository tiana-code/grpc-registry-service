package com.grpcregistry.service;

public class DuplicateContentException extends RuntimeException {
    public DuplicateContentException(String message) {
        super(message);
    }
}
