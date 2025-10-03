package com.lasystems.lagenda.dtos;

import java.util.UUID;

public record ProviderSummary(
        UUID id,
        String name,
        String calendarId
) {
}
