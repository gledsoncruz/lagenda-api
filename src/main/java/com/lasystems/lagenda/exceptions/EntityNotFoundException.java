package com.lasystems.lagenda.exceptions;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException() {
        super("Entidade não encontrada");
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityName, UUID id) {
        super(String.format("%s com ID %s não encontrado", entityName, id));
    }

    public EntityNotFoundException(Class<?> entityClass, UUID id) {
        super(String.format("%s com ID %s não encontrado",
                entityClass.getSimpleName(), id));
    }
}
