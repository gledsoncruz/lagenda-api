package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.dtos.CompanyDto;
import com.lasystems.lagenda.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID>, JpaSpecificationExecutor<Company> {

    @Query("""
    SELECT c
    FROM Company c
    WHERE c.id = :id
    """)
    Optional<CompanyDto> findCompanyDto(@Param("id") UUID id);
}
