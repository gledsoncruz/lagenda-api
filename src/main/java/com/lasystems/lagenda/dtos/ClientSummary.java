package com.lasystems.lagenda.dtos;

import java.util.UUID;

public record ClientSummary(
        UUID id,
        String name,
        String phone
) {
}
