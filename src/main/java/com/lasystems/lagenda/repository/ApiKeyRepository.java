package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.models.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para gerenciar API Keys.
 */
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    /**
     * Busca API Key pelo hash.
     */
    Optional<ApiKey> findByKeyHash(String keyHash);

    /**
     * Busca todas as API Keys ativas de uma empresa.
     */
    @Query("""
        SELECT ak FROM ApiKey ak
        WHERE ak.company.id = :companyId
        AND ak.isActive = true
        ORDER BY ak.createdAt DESC
    """)
    List<ApiKey> findActiveByCompanyId(@Param("companyId") UUID companyId);

    /**
     * Conta API Keys ativas de uma empresa.
     */
    @Query("""
        SELECT COUNT(ak) FROM ApiKey ak
        WHERE ak.company.id = :companyId
        AND ak.isActive = true
    """)
    long countActiveByCompanyId(@Param("companyId") UUID companyId);

    /**
     * Atualiza o último uso da API Key.
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE ApiKey ak
        SET ak.lastUsedAt = CURRENT_TIMESTAMP,
            ak.usageCount = ak.usageCount + 1
        WHERE ak.id = :id
    """)
    void updateUsage(@Param("id") UUID id);

    /**
     * Desativa API Keys expiradas.
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE ApiKey ak
        SET ak.isActive = false
        WHERE ak.expiresAt < CURRENT_TIMESTAMP
        AND ak.isActive = true
    """)
    int deactivateExpiredKeys();
}
