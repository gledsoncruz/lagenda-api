package com.lasystems.lagenda.dtos;

/**
 * Response de validação de API Key.
 */
public record ApiKeyValidationResponse(
        boolean isValid,
        String message
) {}
