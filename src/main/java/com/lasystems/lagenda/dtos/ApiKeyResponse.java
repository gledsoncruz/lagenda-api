package com.lasystems.lagenda.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response ao listar API Keys.
 */
public record ApiKeyResponse(
        UUID id,
        String name,
        String keyPrefix, // Mascarado
        LocalDateTime expiresAt,
        LocalDateTime lastUsedAt,
        Long usageCount,
        Integer rateLimit,
        Boolean isActive
) {}
