package com.lasystems.lagenda.dtos;

import java.util.UUID;

/**
 * Response ao revogar API Key.
 */
public record ApiKeyRevokeResponse(
        UUID id,
        String message
) {}
