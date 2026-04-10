package com.grpcregistry.service.impl;

import com.grpcregistry.model.Document;
import com.grpcregistry.repository.DocumentRepository;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor
public class ContentHashService {

    private final DocumentRepository documentRepository;

    public String computeHash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content);
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public String computeHash(String content) {
        return computeHash(content.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<UUID> findDuplicateDocumentId(String contentHash) {
        return documentRepository.findByContentHash(contentHash)
                .map(Document::getId);
    }

    public boolean verifyHash(String content, String expectedHash) {
        String actualHash = computeHash(content);
        return actualHash.equals(expectedHash);
    }
}
