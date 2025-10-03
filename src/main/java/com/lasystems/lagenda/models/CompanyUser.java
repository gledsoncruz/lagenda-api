package com.lasystems.lagenda.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
* Usuário da empresa (para login no frontend).
*/
@Entity
@Table(name = "company_users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CompanyUser extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId; // Referência ao auth.users do Supabase

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "role", nullable = false)
    private String role; // "admin", "staff"

    /**
     * Verifica se o usuário é administrador.
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}
