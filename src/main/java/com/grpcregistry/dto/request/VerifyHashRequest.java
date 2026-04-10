package com.grpcregistry.dto.request;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record VerifyHashRequest(
        @NotBlank
        String content
) {
}
