package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.models.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository para gerenciar Assinaturas.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    /**
     * Busca assinatura ativa de uma empresa.
     */
    @Query("""
        SELECT s FROM Subscription s
        WHERE s.company.id = :companyId
        AND s.status = 'active'
        ORDER BY s.createdAt DESC
        LIMIT 1
    """)
    Optional<Subscription> findActiveByCompanyId(@Param("companyId") UUID companyId);

    /**
     * Verifica se empresa tem assinatura ativa.
     */
    @Query("""
        SELECT COUNT(s) > 0 FROM Subscription s
        WHERE s.company.id = :companyId
        AND s.status = 'active'
    """)
    boolean hasActiveSubscription(@Param("companyId") UUID companyId);
}
