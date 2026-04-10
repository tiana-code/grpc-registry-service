package com.grpcregistry.dto.request;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Serdeable
public record CreateDocumentRequest(
        @NotBlank @Size(max = 500)
        String title,

        @NotBlank
        String content
) {
}
