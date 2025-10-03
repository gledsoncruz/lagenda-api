package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.models.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository para gerenciar Planos.
 */
public interface PlanRepository extends JpaRepository<Plan, String> {

    /**
     * Busca plano pelo ID do Stripe.
     */
    Optional<Plan> findByStripePriceId(String stripePriceId);
}
