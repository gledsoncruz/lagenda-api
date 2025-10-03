package com.lasystems.lagenda.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        String message,
        UUID appointmentId,
        LocalDateTime scheduledTime
) {
}
