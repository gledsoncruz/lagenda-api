package com.lasystems.lagenda.controllers;

import com.lasystems.lagenda.dtos.*;
import com.lasystems.lagenda.service.ApiKeyService;
import com.lasystems.lagenda.validators.UUIDValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller para bootstrap inicial - criar primeira API Key.
 * IMPORTANTE: Este endpoint deve ser protegido por um secret token
 * ou desabilitado em produção após uso inicial.
 */
@Slf4j
@RestController
@RequestMapping("/bootstrap")
@RequiredArgsConstructor
public class BootstrapController {

    private final ApiKeyService apiKeyService;

    @Value("${bootstrap.secret}")
    private String bootstrapSecret;

    @Value("${bootstrap.enabled}")
    private boolean bootstrapEnabled;

    /**
     * Cria a primeira API Key para uma empresa.
     * Este endpoint NÃO requer autenticação, mas precisa de um secret token.
     * <p>
     * POST /bootstrap/create-first-api-key
     * Header: X-Bootstrap-Secret: seu_secret_aqui
     * Body: {
     * "companyId": "uuid",
     * "name": "First API Key",
     * "bootstrapSecret": "seu_secret_aqui"
     * }
     * <p>
     * SEGURANÇA:
     * - Desabilitar em produção após criar keys iniciais
     * - Usar secret forte (via variável de ambiente)
     * - Monitorar logs para uso suspeito
     */
    @PostMapping("/create-first-api-key")
    public ResponseEntity<?> createFirstApiKey(
            @RequestHeader(value = "X-Bootstrap-Secret", required = false) String headerSecret,
            @RequestBody @Valid CreateFirstApiKeyRequest request
    ) {
        log.warn("Tentativa de criar primeira API Key para company: {}", request.companyId());

        // Verificar se está habilitado
        if (!bootstrapEnabled) {
            log.error("Bootstrap endpoint está desabilitado");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Bootstrap endpoint desabilitado"));
        }

        // Validar secret (header ou body)
        String providedSecret = headerSecret != null ? headerSecret : request.bootstrapSecret();

        if (providedSecret == null || !providedSecret.equals(bootstrapSecret)) {
            log.error("Bootstrap secret inválido fornecido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Bootstrap secret inválido"));
        }

        // Validar se é realmente a primeira key (opcional - remova se quiser permitir múltiplas)
        UUID companyId = UUIDValidator.parseOrThrow(request.companyId(), "companyId");

        try {
            // Criar API Key
            ApiKeyCreationResult result = apiKeyService.createApiKey(
                    companyId,
                    request.name() != null ? request.name() : "Initial API Key",
                    request.expiresInDays() != null ? request.expiresInDays() : 365,
                    true // Sempre produção
            );

            log.info("✅ Primeira API Key criada com sucesso para company: {}", companyId);

            FirstApiKeyResponse response = new FirstApiKeyResponse(
                    result.id(),
                    result.plainKey(),
                    result.keyPrefix(),
                    result.expiresAt(),
                    "⚠️ ATENÇÃO: Guarde esta API Key! Ela só será exibida uma vez!",
                    "Após guardar a key, desabilite este endpoint em produção (bootstrap.enabled=false)"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Erro ao criar primeira API Key: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Verifica se o bootstrap está habilitado.
     * <p>
     * GET /bootstrap/status
     */
    @GetMapping("/status")
    public ResponseEntity<BootstrapStatusResponse> getStatus() {
        return ResponseEntity.ok(new BootstrapStatusResponse(
                bootstrapEnabled,
                bootstrapEnabled
                        ? "Bootstrap está HABILITADO. Desabilite após criar keys iniciais!"
                        : "Bootstrap está desabilitado."
        ));
    }
}
