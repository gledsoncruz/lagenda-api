package com.lasystems.lagenda.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record BestSlotResponse(
        UUID providerId,
        String providerName,
        LocalDateTime startTime
) {
}
