package com.lasystems.lagenda.validators;

import com.lasystems.lagenda.exceptions.UUIDIllegalArgumentException;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utilitário para validação e conversão de UUIDs.
 * Suporta formato padrão RFC 4122: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
 * Exemplo: 39465a8a-38a1-49e4-aeb7-8c892d59e434
 */
public final class UUIDValidator {

    /**
     * Padrão regex para validar formato UUID RFC 4122.
     * Formato: 8-4-4-4-12 caracteres hexadecimais separados por hífen
     */
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private UUIDValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converte String para UUID ou lança exceção customizada.
     *
     * @param uuid String UUID no formato padrão
     * @param fieldName nome do campo (para mensagem de erro)
     * @return UUID válido
     * @throws UUIDIllegalArgumentException se formato inválido
     */
    public static UUID parseOrThrow(String uuid, String fieldName) {
        if (uuid == null) {
            throw new UUIDIllegalArgumentException(
                    String.format("Campo '%s' não pode ser nulo", fieldName)
            );
        }

        // Remove espaços em branco
        uuid = uuid.trim();

        if (uuid.isEmpty()) {
            throw new UUIDIllegalArgumentException(
                    String.format("Campo '%s' não pode ser vazio", fieldName)
            );
        }

        // Valida formato antes de tentar converter
        if (!UUID_PATTERN.matcher(uuid).matches()) {
            throw new UUIDIllegalArgumentException(
                    String.format("Formato inválido de UUID para o campo '%s'. " +
                            "Esperado: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx (ex: 39465a8a-38a1-49e4-aeb7-8c892d59e434). " +
                            "Recebido: %s", fieldName, uuid)
            );
        }

        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new UUIDIllegalArgumentException(
                    String.format("Erro ao converter UUID para o campo '%s': %s. Valor: %s",
                            fieldName, e.getMessage(), uuid)
            );
        }
    }

    /**
     * Converte String para UUID, retornando null se inválido.
     * Útil quando o UUID é opcional.
     *
     * @param uuid String UUID
     * @return UUID válido ou null
     */
    public static UUID parseOrNull(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }

        uuid = uuid.trim();

        if (!UUID_PATTERN.matcher(uuid).matches()) {
            return null;
        }

        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Converte lista de Strings para lista de UUIDs.
     * Lança exceção se algum UUID for inválido.
     *
     * @param uuids lista de strings UUID
     * @param fieldName nome do campo (para mensagem de erro)
     * @return lista de UUIDs válidos
     * @throws UUIDIllegalArgumentException se algum UUID for inválido
     */
    public static List<UUID> parseList(List<String> uuids, String fieldName) {
        if (uuids == null || uuids.isEmpty()) {
            throw new UUIDIllegalArgumentException(
                    String.format("Lista de '%s' não pode ser nula ou vazia", fieldName)
            );
        }

        return uuids.stream()
                .map(uuid -> {
                    if (uuid == null || uuid.trim().isEmpty()) {
                        throw new UUIDIllegalArgumentException(
                                String.format("UUID na lista '%s' não pode ser nulo ou vazio", fieldName)
                        );
                    }
                    return parseOrThrow(uuid.trim(), fieldName);
                })
                .collect(Collectors.toList());
    }

    /**
     * Converte lista de Strings para lista de UUIDs, ignorando inválidos.
     * Útil quando você quer filtrar UUIDs válidos de uma lista mista.
     *
     * @param uuids lista de strings UUID
     * @return lista de UUIDs válidos (pode ser vazia)
     */
    public static List<UUID> parseListSafe(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return List.of();
        }

        return uuids.stream()
                .map(uuid -> uuid != null ? uuid.trim() : null)
                .map(UUIDValidator::parseOrNull)
                .filter(uuid -> uuid != null)
                .collect(Collectors.toList());
    }

    /**
     * Valida se uma String é um UUID válido no formato RFC 4122.
     *
     * @param uuid String a ser validada
     * @return true se for um UUID válido
     */
    public static boolean isValid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return false;
        }

        uuid = uuid.trim();

        // Primeiro valida o padrão regex
        if (!UUID_PATTERN.matcher(uuid).matches()) {
            return false;
        }

        // Depois tenta converter para garantir
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Converte UUID para String em lowercase (padrão).
     *
     * @param uuid UUID a ser convertido
     * @return String em lowercase ou null se UUID for null
     */
    public static String toString(UUID uuid) {
        return uuid != null ? uuid.toString().toLowerCase() : null;
    }

    /**
     * Compara dois UUIDs de forma segura (null-safe).
     *
     * @param uuid1 primeiro UUID
     * @param uuid2 segundo UUID
     * @return true se forem iguais (ambos null também retorna true)
     */
    public static boolean equals(UUID uuid1, UUID uuid2) {
        if (uuid1 == null && uuid2 == null) {
            return true;
        }
        if (uuid1 == null || uuid2 == null) {
            return false;
        }
        return uuid1.equals(uuid2);
    }
}