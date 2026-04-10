package com.grpcregistry.dto.response;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record VerifyHashResponse(boolean valid) {
}
