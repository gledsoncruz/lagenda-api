package com.lasystems.lagenda.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceSummary(
        UUID id,
        String name,
        BigDecimal price,
        Integer durationMinutes
) {
}
