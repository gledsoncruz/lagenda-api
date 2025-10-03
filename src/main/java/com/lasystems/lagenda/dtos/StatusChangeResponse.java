package com.lasystems.lagenda.dtos;

import java.util.UUID;

public record StatusChangeResponse(
        String message,
        UUID appointmentId,
        String newStatus
) {

}
