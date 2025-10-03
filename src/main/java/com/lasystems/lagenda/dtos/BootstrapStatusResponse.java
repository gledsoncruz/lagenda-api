package com.lasystems.lagenda.dtos;

public record BootstrapStatusResponse(
        boolean enabled,
        String message
) {
}
