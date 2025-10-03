package com.lasystems.lagenda.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitário para gerar e validar API Keys seguras.
 *
 * Formato da API Key: lag_live_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 * - lag: prefixo da aplicação
 * - live: ambiente (live/test)
 * - xxxxx: 32 caracteres aleatórios (Base64 URL-safe, pode conter underscore)
 */
public final class ApiKeyGenerator {

    private static final String PREFIX = "lag";
    private static final String ENV_LIVE = "live";
    private static final String ENV_TEST = "test";
    private static final int KEY_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(12);

    private ApiKeyGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Gera uma nova API Key.
     *
     * @param isProduction true para ambiente de produção, false para teste
     * @return API Key no formato: lag_live_xxxxxxxxxxxxxxxxxxxxx
     */
    public static String generate(boolean isProduction) {
        String env = isProduction ? ENV_LIVE : ENV_TEST;
        String randomPart = generateRandomString(KEY_LENGTH);
        return String.format("%s_%s_%s", PREFIX, env, randomPart);
    }

    /**
     * Gera uma API Key de produção.
     */
    public static String generateLive() {
        return generate(true);
    }

    /**
     * Gera uma API Key de teste.
     */
    public static String generateTest() {
        return generate(false);
    }

    /**
     * Gera hash da API Key para armazenamento seguro.
     *
     * @param apiKey API Key em texto plano
     * @return Hash BCrypt da API Key
     */
    public static String hash(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API Key não pode ser nula ou vazia");
        }
        return PASSWORD_ENCODER.encode(apiKey);
    }

    /**
     * Verifica se uma API Key corresponde ao hash armazenado.
     *
     * @param apiKey API Key em texto plano
     * @param hash Hash armazenado no banco
     * @return true se a API Key é válida
     */
    public static boolean matches(String apiKey, String hash) {
        if (apiKey == null || hash == null) {
            return false;
        }
        return PASSWORD_ENCODER.matches(apiKey, hash);
    }

    /**
     * Extrai o prefixo da API Key (primeiros 12 caracteres).
     * Usado para identificação sem expor a key completa.
     *
     * @param apiKey API Key completa
     * @return Prefixo (ex: "lag_live_Iy0")
     */
    public static String extractPrefix(String apiKey) {
        if (apiKey == null || apiKey.length() < 12) {
            return "";
        }
        return apiKey.substring(0, 12);
    }

    /**
     * Valida formato da API Key.
     *
     * @param apiKey API Key a ser validada
     * @return true se o formato é válido
     */
    public static boolean isValidFormat(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        apiKey = apiKey.trim();

        // Formato esperado: lag_live_xxxxx ou lag_test_xxxxx
        // Mínimo: lag_live_x (9 chars) + pelo menos 32 chars aleatórios = 41 chars
        if (apiKey.length() < 41) {
            return false;
        }

        // Verificar prefixo
        if (!apiKey.startsWith(PREFIX + "_")) {
            return false;
        }

        // Verificar ambiente (deve ter live_ ou test_ após o prefixo)
        String afterPrefix = apiKey.substring(PREFIX.length() + 1); // Remove "lag_"

        if (!afterPrefix.startsWith(ENV_LIVE + "_") && !afterPrefix.startsWith(ENV_TEST + "_")) {
            return false;
        }

        // Verificar se tem a parte aleatória
        String expectedPrefix = PREFIX + "_" + (afterPrefix.startsWith(ENV_LIVE) ? ENV_LIVE : ENV_TEST) + "_";
        if (apiKey.length() <= expectedPrefix.length()) {
            return false;
        }

        String randomPart = apiKey.substring(expectedPrefix.length());

        // Parte aleatória deve ter pelo menos 32 caracteres
        if (randomPart.length() < KEY_LENGTH) {
            return false;
        }

        // Verificar se contém apenas caracteres válidos do Base64 URL-safe
        // (letras, números, hífen e underscore)
        if (!randomPart.matches("^[A-Za-z0-9_-]+$")) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se é uma API Key de produção.
     */
    public static boolean isLive(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        return apiKey.startsWith(PREFIX + "_" + ENV_LIVE + "_");
    }

    /**
     * Verifica se é uma API Key de teste.
     */
    public static boolean isTest(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        return apiKey.startsWith(PREFIX + "_" + ENV_TEST + "_");
    }

    /**
     * Mascara a API Key para exibição segura.
     * Exibe apenas o prefixo e últimos 4 caracteres.
     *
     * @param apiKey API Key completa
     * @return API Key mascarada (ex: "lag_live_***...***FIh")
     */
    public static String mask(String apiKey) {
        if (apiKey == null || apiKey.length() < 16) {
            return "***";
        }

        // Mostrar lag_live_ e últimos 4 caracteres
        String prefix = apiKey.substring(0, Math.min(12, apiKey.length()));
        String suffix = apiKey.substring(apiKey.length() - 4);
        return prefix + "***...***" + suffix;
    }

    /**
     * Gera string aleatória segura usando Base64 URL-safe encoding.
     * IMPORTANTE: Base64 URL-safe pode conter underscores (_) e hífens (-).
     */
    private static String generateRandomString(int length) {
        // Gerar mais bytes para garantir comprimento após encoding
        byte[] randomBytes = new byte[(int) Math.ceil(length * 0.75) + 1];
        SECURE_RANDOM.nextBytes(randomBytes);

        String encoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Garantir exatamente o comprimento desejado
        return encoded.substring(0, Math.min(length, encoded.length()));
    }
}