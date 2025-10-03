package com.lasystems.lagenda.exceptions;

import java.util.UUID;

/**
 * Exceção lançada quando nenhum prestador está disponível.
 */
public class NoProviderAvailableException extends RuntimeException {

    public NoProviderAvailableException(UUID specialtyId) {
        super(String.format("Nenhum prestador disponível para a especialidade %s", specialtyId));
    }

    public NoProviderAvailableException(String message) {
        super(message);
    }
}
