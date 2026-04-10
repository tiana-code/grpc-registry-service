package com.grpcregistry.dto.request;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Size;

@Serdeable
public record StatusTransitionRequest(
        @Size(max = 500)
        String reason
) {
}
