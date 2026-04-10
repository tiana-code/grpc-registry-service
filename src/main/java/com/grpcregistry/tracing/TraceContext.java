package com.grpcregistry.tracing;

import java.util.UUID;

public record TraceContext(
        String traceId,
        String spanId,
        String parentSpanId
) {
    public static TraceContext root(String traceId) {
        return new TraceContext(traceId, generateSpanId(), null);
    }

    public TraceContext childSpan() {
        return new TraceContext(traceId, generateSpanId(), spanId);
    }

    public static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
