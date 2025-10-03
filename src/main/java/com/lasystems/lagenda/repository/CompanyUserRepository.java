package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.models.CompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
* Repository para gerenciar Usuários da Empresa.
*/
public interface CompanyUserRepository extends JpaRepository<CompanyUser, UUID> {

    /**
     * Busca usuário pelo userId do Supabase.
     */
    Optional<CompanyUser> findByUserId(UUID userId);

    /**
     * Busca todos os usuários de uma empresa.
     */
    List<CompanyUser> findByCompanyId(UUID companyId);

    /**
     * Verifica se usuário pertence à empresa.
     */
    @Query("""
        SELECT COUNT(cu) > 0 FROM CompanyUser cu
        WHERE cu.userId = :userId
        AND cu.company.id = :companyId
    """)
    boolean existsByUserIdAndCompanyId(
            @Param("userId") UUID userId,
            @Param("companyId") UUID companyId
    );
}
