package com.lasystems.lagenda.service;

import com.lasystems.lagenda.dtos.ApiKeyCreationResult;
import com.lasystems.lagenda.exceptions.EntityNotFoundException;
import com.lasystems.lagenda.models.ApiKey;
import com.lasystems.lagenda.models.Company;
import com.lasystems.lagenda.models.Subscription;
import com.lasystems.lagenda.repository.ApiKeyRepository;
import com.lasystems.lagenda.repository.CompanyRepository;
import com.lasystems.lagenda.repository.SubscriptionRepository;
import com.lasystems.lagenda.util.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service para gerenciar API Keys.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final CompanyRepository companyRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * Cria uma nova API Key para a empresa.
     *
     * @param companyId     ID da empresa
     * @param name          Nome descritivo da key
     * @param expiresInDays Dias até expirar (null = sem expiração)
     * @param isProduction  true para key de produção
     * @return Objeto com a API Key em texto plano (única vez que será visível)
     */
    @Transactional
    public ApiKeyCreationResult createApiKey(
            UUID companyId,
            String name,
            Integer expiresInDays,
            boolean isProduction
    ) {
        log.info("Criando API Key para company: {}", companyId);

        // Validar empresa
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company", companyId));

        // Validar assinatura ativa
        Subscription subscription = subscriptionRepository.findActiveByCompanyId(companyId)
                .orElseThrow(() -> new IllegalStateException("Empresa não possui assinatura ativa"));

        // Validar limite de API Keys do plano
        long currentKeyCount = apiKeyRepository.countActiveByCompanyId(companyId);
        int maxKeys = subscription.getPlan().getMaxApiKeys();

        if (currentKeyCount >= maxKeys) {
            throw new IllegalStateException(
                    String.format("Limite de %d API Keys atingido para o plano %s",
                            maxKeys, subscription.getPlan().getName())
            );
        }

        // Gerar API Key
        String plainKey = isProduction
                ? ApiKeyGenerator.generateLive()
                : ApiKeyGenerator.generateTest();

        String keyHash = ApiKeyGenerator.hash(plainKey);
        String keyPrefix = ApiKeyGenerator.extractPrefix(plainKey);

        // Calcular expiração
        LocalDateTime expiresAt = expiresInDays != null
                ? LocalDateTime.now().plusDays(expiresInDays)
                : null;

        // Criar e salvar API Key
        ApiKey apiKey = ApiKey.builder()
                .company(company)
                .keyHash(keyHash)
                .keyPrefix(keyPrefix)
                .name(name)
                .expiresAt(expiresAt)
                .isActive(true)
                .rateLimit(subscription.getPlan().getRateLimitPerMinute())
                .usageCount(0L)
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        log.info("API Key {} criada com sucesso para company {}", apiKey.getId(), companyId);

        return new ApiKeyCreationResult(
                apiKey.getId(),
                plainKey, // IMPORTANTE: Retornar apenas uma vez!
                keyPrefix,
                expiresAt
        );
    }

    /**
     * Valida uma API Key e retorna os dados associados.
     *
     * @param plainKey API Key em texto plano
     * @return API Key validada
     */
    @Transactional
    public Optional<ApiKey> validateApiKey(String plainKey) {
        if (!ApiKeyGenerator.isValidFormat(plainKey)) {
            log.warn("API Key com formato inválido recebida");
            return Optional.empty();
        }

        // Buscar por hash
        List<ApiKey> allKeys = apiKeyRepository.findAll();

        for (ApiKey apiKey : allKeys) {
            if (ApiKeyGenerator.matches(plainKey, apiKey.getKeyHash())) {
                if (!apiKey.isValid()) {
                    log.warn("API Key {} está inativa ou expirada", apiKey.getId());
                    return Optional.empty();
                }

                // Validar assinatura da empresa
                boolean hasActiveSubscription = subscriptionRepository
                        .hasActiveSubscription(apiKey.getCompany().getId());

                if (!hasActiveSubscription) {
                    log.warn("API Key {} pertence a empresa sem assinatura ativa", apiKey.getId());
                    return Optional.empty();
                }

                // Atualizar uso
                apiKey.recordUsage();
                apiKeyRepository.save(apiKey);

                log.debug("API Key {} validada com sucesso", apiKey.getId());
                return Optional.of(apiKey);
            }
        }

        log.warn("API Key não encontrada ou inválida");
        return Optional.empty();
    }

    /**
     * Lista API Keys ativas de uma empresa.
     */
    @Transactional(readOnly = true)
    public List<ApiKey> listActiveKeys(UUID companyId) {
        return apiKeyRepository.findActiveByCompanyId(companyId);
    }

    /**
     * Revoga (desativa) uma API Key.
     */
    @Transactional
    public void revokeApiKey(UUID keyId, UUID companyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new EntityNotFoundException("ApiKey", keyId));

        // Validar que a key pertence à empresa
        if (!apiKey.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("API Key não pertence a esta empresa");
        }

        apiKey.setIsActive(false);
        apiKeyRepository.save(apiKey);

        log.info("API Key {} revogada", keyId);
    }

    /**
     * Job agendado para desativar API Keys expiradas.
     * Executa diariamente às 2h da manhã.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deactivateExpiredKeys() {
        log.info("Executando job de desativação de API Keys expiradas");
        int count = apiKeyRepository.deactivateExpiredKeys();
        log.info("Total de {} API Keys expiradas desativadas", count);
    }
}
