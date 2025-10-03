package com.lasystems.lagenda.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.beans.Transient;
import java.time.Instant;

/**
 * Plano de assinatura da empresa.
 */
@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Plan {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private String id; // "free", "basic", "premium"

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "stripe_price_id", unique = true)
    private String stripePriceId;

    @Column(name = "created_at")
    private Instant createdAt;

    // Limites do plano
    @Transient
    public int getMaxAppointmentsPerMonth() {
        return switch (id) {
            case "free" -> 50;
            case "basic" -> 500;
            case "premium" -> -1; // ilimitado
            default -> 0;
        };
    }

    @Transient
    public int getMaxApiKeys() {
        return switch (id) {
            case "free" -> 1;
            case "basic" -> 3;
            case "premium" -> 10;
            default -> 0;
        };
    }

    @Transient
    public int getRateLimitPerMinute() {
        return switch (id) {
            case "free" -> 10;
            case "basic" -> 60;
            case "premium" -> 300;
            default -> 5;
        };
    }
}
