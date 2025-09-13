package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.dtos.BusinessHourDto;
import com.lasystems.lagenda.models.BusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BusinessHourRepository extends JpaRepository<BusinessHour, UUID>, JpaSpecificationExecutor<BusinessHour> {

//    @Query("""
//    SELECT c.company as company, c.dayOfWeek as dayOfWeek, c.startTime as startTime, c.endTime as endTime
//    FROM BusinessHour c
//    WHERE c.company.id = :id
//    """)
//    List<BusinessHourDto> findBusinessHoursByCompany(@Param("id") UUID id);

    @Query("SELECT b FROM BusinessHour b WHERE b.company.id = :companyId AND b.dayOfWeek = :dayOfWeek")
    List<BusinessHour> findByCompanyIdAndDayOfWeek(@Param("companyId") UUID companyId, @Param("dayOfWeek") Integer dayOfWeek);

    List<BusinessHour> findByCompanyId(UUID companyId);



}
