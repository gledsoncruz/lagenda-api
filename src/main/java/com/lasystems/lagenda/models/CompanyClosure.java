package com.lasystems.lagenda.models;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Table(name = "company_closures")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
public class CompanyClosure extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Company company;
    @Column(name = "date")
    private LocalDate date;
    @Column(name = "reason", columnDefinition = "text")
    private String reason;
    @Column(name = "start_time")
    private LocalTime startTime; // opcional: ex: 12:00
    @Column(name = "end_time")
    private LocalTime endTime; // opcional: ex: 14:00

    /**
     * Verifica se este fechamento se sobrepõe a um agendamento.
     *
     * @param start Início do agendamento (LocalDateTime)
     * @param end   Fim do agendamento (LocalDateTime)
     * @return true se houver sobreposição
     */
    public boolean overlapsWith(LocalDateTime start, LocalDateTime end) {
        LocalDate closureDate = this.date;

        // Se o agendamento não for no mesmo dia, não há sobreposição
        if (!start.toLocalDate().equals(closureDate)) {
            return false;
        }

        // Define o início e fim do fechamento como LocalDateTime
        LocalDateTime closureStart;
        LocalDateTime closureEnd;

        if (startTime != null && endTime != null) {
            // Fechamento com horário específico: ex: 12:00 - 14:00
            closureStart = closureDate.atTime(startTime);
            closureEnd = closureDate.atTime(endTime);
        } else {
            // Fechamento de dia inteiro: 00:00 - 23:59:59
            closureStart = closureDate.atStartOfDay();
            closureEnd = closureDate.atTime(23, 59, 59);
        }

        // Verifica sobreposição
        return !end.isBefore(closureStart) && !start.isAfter(closureEnd);
    }

}
