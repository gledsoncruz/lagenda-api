package com.lasystems.lagenda.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Assinatura da empresa a um plano.
 */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Subscription extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @Column(name = "status", nullable = false)
    private String status; // "active", "canceled", "past_due", "unpaid"

    /**
     * Verifica se a assinatura está ativa.
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }
}
