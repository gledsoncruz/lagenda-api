package com.lasystems.lagenda.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateFirstApiKeyRequest(
        @NotBlank(message = "companyId é obrigatório")
        String companyId,

        String name,
        Integer expiresInDays,
        String bootstrapSecret
) {}
