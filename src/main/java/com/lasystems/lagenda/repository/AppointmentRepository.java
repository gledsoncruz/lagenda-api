package com.lasystems.lagenda.repository;

import com.lasystems.lagenda.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {

    /**
     * Verifica se existe agendamento conflitante.
     *
     * @param companyId   ID da empresa
     * @param providerId  ID do prestador (pode ser null)
     * @param start       Início do novo agendamento
     * @param end         Fim do novo agendamento
     * @return true se HOUVER conflito (ou seja, NÃO está disponível)
     */
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END
        FROM Appointment a
        WHERE a.company.id = :companyId
          AND (:providerId IS NULL OR a.provider.id = :providerId)
          AND a.status NOT IN ('CANCELLED', 'COMPLETED')
          AND a.start < :end
          AND a.end > :start
        """)
    boolean hasOverlappingAppointment(
            @Param("companyId") UUID companyId,
            @Param("providerId") UUID providerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.provider.id = :providerId AND a.start >= :startOfDay AND a.start < :nextStartOfDay")
    int countAppointmentsByProviderAndDate(
            @Param("providerId") UUID providerId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("nextStartOfDay") LocalDateTime nextStartOfDay
    );

    @Query("SELECT a FROM Appointment a WHERE FUNCTION('DATE', a.start) < current_date AND a.status = 'SCHEDULED'")
    List<Appointment> pastAppointmentsScheduled();

    @Transactional
    @Modifying
    @Query("UPDATE Appointment a SET a.status = 'CANCELLED' WHERE a.id IN :ids")
    void cancelAppointmentsBatch(@Param("ids") List<UUID> ids);

    @Query("""
    SELECT COUNT(a) > 0 FROM Appointment a
    WHERE a.client.id = :clientId
      AND a.status IN ('SCHEDULED', 'CONFIRMED')
      AND a.start < :end
      AND a.end > :start
    """)
    boolean hasOverlappingAppointmentForClient(
            @Param("clientId") UUID clientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
