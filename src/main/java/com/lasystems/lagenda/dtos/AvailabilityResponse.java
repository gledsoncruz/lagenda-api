package com.lasystems.lagenda.dtos;

import java.time.LocalDateTime;

public record AvailabilityResponse(
        String message,
        boolean available,
        LocalDateTime requestedTime
) {
}
