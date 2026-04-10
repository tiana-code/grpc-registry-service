package com.grpcregistry.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum DocumentStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED,
    REVOKED;

    private static final Map<DocumentStatus, Set<DocumentStatus>> ALLOWED_TRANSITIONS = Map.of(
            DRAFT, EnumSet.of(PUBLISHED),
            PUBLISHED, EnumSet.of(ARCHIVED, REVOKED),
            ARCHIVED, EnumSet.noneOf(DocumentStatus.class),
            REVOKED, EnumSet.noneOf(DocumentStatus.class)
    );

    public boolean canTransitionTo(DocumentStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(DocumentStatus.class)).contains(target);
    }
}
