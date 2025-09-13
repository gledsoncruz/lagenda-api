package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.dtos.ProviderAvailableDto;
import com.lasystems.lagenda.dtos.ProviderDto;
import com.lasystems.lagenda.dtos.ProviderMinAppointmentsDto;
import com.lasystems.lagenda.models.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<Provider, UUID>, JpaSpecificationExecutor<Provider> {

    @Query("""
    SELECT c.name as name, c.calendarId as calendarId, c.phone as phone
    FROM Provider c
    LEFT JOIN c.appointments a
    WHERE c.company.id = :id
    """)
    List<ProviderDto> findProvidersByCompany(@Param("id") UUID id);

    @Query(value = """
        SELECT p.id AS id, p.calendar_id AS calendarId
            FROM providers p
                LEFT JOIN appointments a ON a.provider_id = p.id
                    AND a.status IN ('SCHEDULED', 'CONFIRMED')
                    AND (a.start_appointment AT TIME ZONE 'America/Sao_Paulo')::date = :date
                    AND a.company_id = :companyId
                INNER JOIN provider_specialties ps ON p.id = ps.provider_id AND ps.specialty_id = :specialtyId
            WHERE p.company_id = :companyId
            GROUP BY p.id, p.calendar_id
            ORDER BY COUNT(a.id) ASC
            LIMIT 1
        """, nativeQuery = true)
    Optional<ProviderMinAppointmentsDto> findProviderWithLeastAppointments(
            @Param("companyId") UUID companyId,
            @Param("specialtyId") UUID specialtyId,
            @Param("date") LocalDate date
    );

//    @Query(
//            value = """
//        SELECT p.id AS id, p.name AS name, p.calendarId AS calendarId
//        FROM Provider p
//        WHERE p.company.id = :companyId
//          AND NOT EXISTS (
//            SELECT 1
//            FROM Appointment a
//            WHERE a.provider.id = p.id
//              AND a.status IN ('SCHEDULED', 'CONFIRMED')
//              AND a.start < :endTime
//              AND a.end > :startTime
//          )
//          AND EXISTS (
//            SELECT 1
//            FROM ProviderSpecialty ps
//            WHERE ps.provider.id = p.id
//              AND ps.specialty.id = :specialtyId
//          )
//        """,
//            nativeQuery = false // JPQL
//    )
//    List<ProviderAvailableDto> findAvailableProviders(
//            @Param("companyId") UUID companyId,
//            @Param("specialtyId") UUID specialtyId,
//            @Param("startTime") LocalDateTime startTime,
//            @Param("endTime") LocalDateTime endTime
//    );

    @Query("""
        SELECT p FROM Provider p
        JOIN ProviderSpecialist ps ON ps.provider = p AND ps.specialist.id = :specialtyId
        WHERE p.company.id = :companyId
          AND NOT EXISTS (
            SELECT 1 FROM Appointment a
            WHERE a.provider = p
              AND a.status IN ('SCHEDULED', 'CONFIRMED')
              AND a.start < :end
              AND a.end > :start
          )
        """)
    List<Provider> findAvailableProviders(
            @Param("companyId") UUID companyId,
            @Param("specialtyId") UUID specialtyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT p FROM Provider p
        JOIN ProviderSpecialist ps ON ps.provider = p AND ps.specialist.id = :specialtyId
        WHERE p.company.id = :companyId
        """)
    List<Provider> findProvidersBySpecialty(
            @Param("companyId") UUID companyId,
            @Param("specialtyId") UUID specialtyId
    );
}
