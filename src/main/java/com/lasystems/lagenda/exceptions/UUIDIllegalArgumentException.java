package com.lasystems.lagenda.exceptions;

/**
 * Exceção lançada quando um UUID inválido é fornecido.
 * Melhorada para incluir mensagens mais descritivas.
 */
public class UUIDIllegalArgumentException extends RuntimeException {

    public UUIDIllegalArgumentException() {
        super("Formato de UUID inválido");
    }

    public UUIDIllegalArgumentException(String message) {
        super(message);
    }

    public UUIDIllegalArgumentException(String fieldName, String value) {
        super(String.format("Formato inválido de UUID para o campo '%s': %s", fieldName, value));
    }
}