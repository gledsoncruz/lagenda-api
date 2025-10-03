package com.lasystems.lagenda.dtos;

import java.util.UUID;

public record ServiceSummary(
        UUID id,
        String name,
        Double price,
        Integer durationMinutes
) {
}
