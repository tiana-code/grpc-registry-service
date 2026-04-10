package com.grpcregistry.event;

import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class DocumentEventPublisher {

    private final ApplicationEventPublisher<DocumentEvent> applicationEventPublisher;

    public void publish(DocumentEvent event) {
        log.debug("Publishing event: type={} documentId={}", event.getClass().getSimpleName(), event.documentId());
        applicationEventPublisher.publishEvent(event);
    }
}
