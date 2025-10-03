package com.lasystems.lagenda.dtos;

import java.util.List;
import java.util.UUID;

public record FinalizeMissedAppointmentsResponse(
        String message,
        int totalUpdated,
        List<UUID> updatedIds
) {
}
