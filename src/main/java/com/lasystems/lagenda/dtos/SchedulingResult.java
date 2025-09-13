package com.lasystems.lagenda.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record SchedulingResult(UUID providerId, LocalDateTime startTime) {
}
