package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.dtos.CompanyClosureDto;
import com.lasystems.lagenda.models.CompanyClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CompanyClosureRepository extends JpaRepository<CompanyClosure, UUID>, JpaSpecificationExecutor<CompanyClosure> {

//    @Query("""
//    SELECT c.date as date, c.reason as reason, c.startTime as startTime, c.endTime as endTime
//    FROM CompanyClosure c
//    WHERE c.company.id = :id
//    """)
//    List<CompanyClosureDto> findCompanyClosureByCompany(@Param("id") UUID id);
@Query("SELECT c FROM CompanyClosure c WHERE c.company.id = :companyId AND c.date = :date")
List<CompanyClosure> findByCompanyIdAndDate(@Param("companyId") UUID companyId, @Param("date") LocalDate date);

List<CompanyClosure> findByCompanyId(UUID companyId);
}
