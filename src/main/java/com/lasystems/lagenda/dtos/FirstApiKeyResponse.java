package com.lasystems.lagenda.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record FirstApiKeyResponse(
        UUID id,
        String plainKey,
        String keyPrefix,
        LocalDateTime expiresAt,
        String warning,
        String securityNote
) {
}
