package com.lasystems.lagenda.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API Key para integração com N8N e outras APIs externas.
 * Permite autenticação máquina-a-máquina sem necessidade de login.
 */
@Entity
@Table(name = "api_keys")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ApiKey extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "key_hash", nullable = false, unique = true)
    private String keyHash;

    @Column(name = "key_prefix", nullable = false)
    private String keyPrefix; // Primeiros 8 caracteres para identificação

    @Column(name = "name", nullable = false)
    private String name; // Nome descritivo (ex: "N8N Integration", "Webhook Bot")

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "permissions", columnDefinition = "text[]")
    private String[] permissions; // Ex: ["appointments:create", "appointments:read"]

    @Column(name = "rate_limit")
    private Integer rateLimit; // Requisições por minuto

    @Column(name = "usage_count")
    @Builder.Default
    private Long usageCount = 0L;

    /**
     * Verifica se a API Key está ativa e não expirada.
     */
    public boolean isValid() {
        if (!isActive) {
            return false;
        }
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    /**
     * Incrementa contador de uso e atualiza último uso.
     */
    public void recordUsage() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
}
