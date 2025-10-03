package com.lasystems.lagenda.exceptions;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Exceção lançada quando um cliente já possui agendamento no horário solicitado.
 */
public class ClientConflictException extends RuntimeException {
    public ClientConflictException(UUID clientId, LocalDateTime start) {
        super(String.format("Cliente %s já possui agendamento no horário %s", clientId, start));
    }
}
