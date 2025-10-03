package com.lasystems.lagenda.controllers;

import com.lasystems.lagenda.dtos.*;
import com.lasystems.lagenda.models.ApiKey;
import com.lasystems.lagenda.service.ApiKeyService;
import com.lasystems.lagenda.util.ApiKeyGenerator;
import com.lasystems.lagenda.validators.UUIDValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller para gerenciar API Keys.
 * Permite criar, listar e revogar chaves de API.
 */
@Slf4j
@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    /**
     * Cria uma nova API Key.
     * <p>
     * POST /api/keys
     * Body: {
     * "companyId": "uuid",
     * "name": "N8N Integration",
     * "expiresInDays": 365,
     * "isProduction": true
     * }
     */
    @PostMapping
    public ResponseEntity<ApiKeyCreationResponse> createApiKey(
            @RequestBody @Valid CreateApiKeyRequest request
    ) {
        log.info("Criando API Key: {} para company: {}", request.name(), request.companyId());

        UUID companyId = UUIDValidator.parseOrThrow(request.companyId(), "companyId");

        ApiKeyCreationResult result = apiKeyService.createApiKey(
                companyId,
                request.name(),
                request.expiresInDays(),
                request.isProduction() != null ? request.isProduction() : true
        );

        ApiKeyCreationResponse response = new ApiKeyCreationResponse(
                result.id(),
                result.plainKey(),
                result.keyPrefix(),
                result.expiresAt(),
                "API Key criada com sucesso. ATENÇÃO: Esta é a única vez que a chave será exibida!"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista API Keys ativas de uma empresa.
     * <p>
     * GET /api/keys?companyId=uuid
     */
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys(
            @RequestParam String companyId
    ) {
        log.debug("Listando API Keys para company: {}", companyId);

        UUID id = UUIDValidator.parseOrThrow(companyId, "companyId");
        List<ApiKey> keys = apiKeyService.listActiveKeys(id);

        List<ApiKeyResponse> response = keys.stream()
                .map(key -> new ApiKeyResponse(
                        key.getId(),
                        key.getName(),
                        ApiKeyGenerator.mask(key.getKeyPrefix()), // Exibir apenas prefixo mascarado
                        key.getExpiresAt(),
                        key.getLastUsedAt(),
                        key.getUsageCount(),
                        key.getRateLimit(),
                        key.getIsActive()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Revoga (desativa) uma API Key.
     * <p>
     * DELETE /api/keys/{keyId}?companyId=uuid
     */
    @DeleteMapping("/{keyId}")
    public ResponseEntity<ApiKeyRevokeResponse> revokeApiKey(
            @PathVariable String keyId,
            @RequestParam String companyId
    ) {
        log.info("Revogando API Key: {} para company: {}", keyId, companyId);

        UUID id = UUIDValidator.parseOrThrow(keyId, "keyId");
        UUID cId = UUIDValidator.parseOrThrow(companyId, "companyId");

        apiKeyService.revokeApiKey(id, cId);

        return ResponseEntity.ok(new ApiKeyRevokeResponse(
                id,
                "API Key revogada com sucesso"
        ));
    }

    /**
     * Endpoint para testar se uma API Key é válida.
     * Útil para debugging.
     * <p>
     * GET /api/keys/validate
     * Header: X-API-Key: lag_live_xxxxx
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiKeyValidationResponse> validateApiKey(
            @RequestHeader("X-API-Key") String apiKey
    ) {
        log.debug("Validando API Key");

        boolean isValid = apiKeyService.validateApiKey(apiKey).isPresent();

        return ResponseEntity.ok(new ApiKeyValidationResponse(
                isValid,
                isValid ? "API Key válida" : "API Key inválida ou expirada"
        ));
    }
}