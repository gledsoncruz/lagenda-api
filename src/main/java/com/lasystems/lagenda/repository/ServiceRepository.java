package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.models.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID>, JpaSpecificationExecutor<Service> {

    @Query("""
        SELECT ssp.specialist.id
        FROM ServiceSpecialties ssp
        WHERE ssp.service.id IN :serviceIds
          AND ssp.specialist.company.id = :companyId
        GROUP BY ssp.specialist.id
        ORDER BY COUNT(ssp) DESC
        """)
    List<UUID> findSpecialtyIdsByServiceIds(@Param("serviceIds") List<UUID> serviceIds, @Param("companyId") UUID companyId);

    default UUID findCommonSpecialtyForServices(List<UUID> serviceIds, UUID companyId) {
        List<UUID> specialtyIds = findSpecialtyIdsByServiceIds(serviceIds, companyId);
        return specialtyIds.isEmpty() ? null : specialtyIds.get(0);
    }

    @Query("SELECT SUM(s.durationMinutes) FROM Service s WHERE s.id IN :serviceIds")
    Integer getTotalDuration(@Param("serviceIds") List<UUID> serviceIds);
}
