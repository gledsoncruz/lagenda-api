package com.lasystems.lagenda.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Request para criar API Key.
 */
public record CreateApiKeyRequest(
        @NotBlank(message = "companyId é obrigatório")
        String companyId,

        @NotBlank(message = "name é obrigatório")
        String name,

        @Positive(message = "expiresInDays deve ser positivo")
        Integer expiresInDays,

        Boolean isProduction
) {}