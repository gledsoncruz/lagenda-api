package com.lasystems.lagenda.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response ao criar API Key.
 * IMPORTANTE: plainKey só é retornado uma vez!
 */
public record ApiKeyCreationResponse(
        UUID id,
        String plainKey,
        String keyPrefix,
        LocalDateTime expiresAt,
        String warning
) {}
