package com.grpcregistry;

import com.grpcregistry.service.impl.ContentHashService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@MicronautTest
class ContentHashServiceTest {

    @Inject
    ContentHashService contentHashService;

    @Test
    void computeHash_producesConsistentSha256() {
        String hash1 = contentHashService.computeHash("hello");
        String hash2 = contentHashService.computeHash("hello");

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64);
    }

    @Test
    void computeHash_producesKnownSha256ForHello() {
        String hash = contentHashService.computeHash("hello");

        assertThat(hash).isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
    }

    @Test
    void computeHash_differentInputsProduceDifferentHashes() {
        String hash1 = contentHashService.computeHash("document A");
        String hash2 = contentHashService.computeHash("document B");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void verifyHash_returnsTrueForCorrectContent() {
        String hash = contentHashService.computeHash("test document");

        assertThat(contentHashService.verifyHash("test document", hash)).isTrue();
    }

    @Test
    void verifyHash_returnsFalseForTamperedContent() {
        String hash = contentHashService.computeHash("original");

        assertThat(contentHashService.verifyHash("tampered", hash)).isFalse();
    }

    @Test
    void findDuplicateDocumentId_returnsEmptyWhenNoMatch() {
        assertThat(contentHashService.findDuplicateDocumentId("abc123")).isEmpty();
    }
}
