package com.lasystems.lagenda.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AppointmentDetailResponse(
        UUID id,
        ClientSummary client,
        ProviderSummary provider,
        LocalDateTime start,
        LocalDateTime end,
        String status,
        List<ServiceSummary> services,
        String notes
) {
}
