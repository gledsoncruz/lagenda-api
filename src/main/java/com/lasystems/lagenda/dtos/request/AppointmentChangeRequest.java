package com.lasystems.lagenda.dtos.request;

import java.time.LocalDateTime;

public record AppointmentChangeRequest(
        String appointmentId,
        LocalDateTime start
) {
}
