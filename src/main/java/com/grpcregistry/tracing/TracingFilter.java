package com.grpcregistry.tracing;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ServerFilter("/api/**")
@Slf4j
public class TracingFilter {

    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    public static final String HEADER_SPAN_ID = "X-Span-Id";
    public static final String HEADER_PARENT_SPAN_ID = "X-Parent-Span-Id";

    static final ThreadLocal<TraceContext> CURRENT_TRACE = new ThreadLocal<>();

    @RequestFilter
    public void onRequest(HttpRequest<?> request) {
        String incomingTraceId = request.getHeaders().get(HEADER_TRACE_ID);
        String incomingParentSpanId = request.getHeaders().get(HEADER_PARENT_SPAN_ID);

        String traceId = (incomingTraceId != null && !incomingTraceId.isBlank())
                ? incomingTraceId
                : UUID.randomUUID().toString();

        TraceContext context = (incomingParentSpanId != null && !incomingParentSpanId.isBlank())
                ? new TraceContext(traceId, TraceContext.generateSpanId(), incomingParentSpanId)
                : TraceContext.root(traceId);

        CURRENT_TRACE.set(context);

        log.debug("Trace started: traceId={} spanId={} parentSpanId={} path={}",
                context.traceId(), context.spanId(), context.parentSpanId(), request.getUri().getPath());
    }

    @ResponseFilter
    public void onResponse(HttpRequest<?> request, MutableHttpResponse<?> response) {
        TraceContext context = CURRENT_TRACE.get();
        if (context != null) {
            response.header(HEADER_TRACE_ID, context.traceId());
            response.header(HEADER_SPAN_ID, context.spanId());
            if (context.parentSpanId() != null) {
                response.header(HEADER_PARENT_SPAN_ID, context.parentSpanId());
            }
            log.debug("Trace completed: traceId={} spanId={} status={}",
                    context.traceId(), context.spanId(), response.getStatus().getCode());
        }
        CURRENT_TRACE.remove();
    }

    public static TraceContext current() {
        return CURRENT_TRACE.get();
    }
}
