package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.models.Specialist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpecialtyRepository extends JpaRepository<Specialist, UUID> {

    @Query("SELECT s FROM Specialist s WHERE s.id = :id AND s.company.id = :companyId")
    Optional<Specialist> findByIdAndCompany(@Param("id") UUID id, @Param("companyId") UUID companyId);

    @Query("SELECT COUNT(s) > 0 FROM Specialist s WHERE s.id = :id AND s.company.id = :companyId")
    boolean existsByIdAndCompany(@Param("id") UUID id, @Param("companyId") UUID companyId);
}
