package com.grpcregistry;

import com.grpcregistry.tracing.TraceContext;
import com.grpcregistry.tracing.TracingFilter;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;

import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@MicronautTest
class TracingFilterTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void request_withoutTraceId_generatesNewTraceIdInResponse() {
        HttpResponse<?> response = client.toBlocking()
            .exchange(HttpRequest.GET("/api/v1/documents"));

        assertThat(response.header(TracingFilter.HEADER_TRACE_ID)).isNotBlank();
        assertThat(response.header(TracingFilter.HEADER_SPAN_ID)).isNotBlank();
    }

    @Test
    void request_withExistingTraceId_propagatesTraceId() {
        HttpResponse<?> response = client.toBlocking()
            .exchange(HttpRequest.GET("/api/v1/documents")
                .header(TracingFilter.HEADER_TRACE_ID, "test-trace-id-12345"));

        assertThat(response.header(TracingFilter.HEADER_TRACE_ID)).isEqualTo("test-trace-id-12345");
    }

    @Test
    void request_withParentSpanId_propagatesViaCorrectHeader() {
        HttpResponse<?> response = client.toBlocking()
            .exchange(HttpRequest.GET("/api/v1/documents")
                .header(TracingFilter.HEADER_PARENT_SPAN_ID, "parent-span-001"));

        assertThat(response.header(TracingFilter.HEADER_PARENT_SPAN_ID)).isEqualTo("parent-span-001");
    }

    @Test
    void postEndpoint_returnsTraceHeaders() {
        HttpResponse<?> response = client.toBlocking()
            .exchange(HttpRequest.POST("/api/v1/documents",
                    "{\"title\":\"Trace Test\",\"content\":\"trace content unique " + System.nanoTime() + "\"}")
                .contentType(MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.header(TracingFilter.HEADER_TRACE_ID)).isNotBlank();
        assertThat(response.header(TracingFilter.HEADER_SPAN_ID)).isNotBlank();
    }

    @Test
    void traceContext_record_childSpanHasSameTraceId() {
        TraceContext root =
            TraceContext.root("my-trace-id");
        TraceContext child = root.childSpan();

        assertThat(child.traceId()).isEqualTo("my-trace-id");
        assertThat(child.parentSpanId()).isEqualTo(root.spanId());
        assertThat(child.spanId()).isNotEqualTo(root.spanId());
    }
}
