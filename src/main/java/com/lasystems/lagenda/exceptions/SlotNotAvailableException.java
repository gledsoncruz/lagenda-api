package com.lasystems.lagenda.exceptions;

import java.time.LocalDateTime;

/**
 * Exceção lançada quando um horário não está disponível para agendamento.
 */
public class SlotNotAvailableException extends RuntimeException {

    public SlotNotAvailableException(LocalDateTime start) {
        super(String.format("Horário %s não está disponível para agendamento", start));
    }

    public SlotNotAvailableException(LocalDateTime start, String reason) {
        super(String.format("Horário %s não disponível: %s", start, reason));
    }
}

