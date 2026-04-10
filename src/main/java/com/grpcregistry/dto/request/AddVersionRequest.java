package com.grpcregistry.dto.request;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Serdeable
public record AddVersionRequest(
        @NotBlank
        String content,

        @Size(max = 1000)
        String changelog,

        @NotBlank
        String createdBy
) {
}
