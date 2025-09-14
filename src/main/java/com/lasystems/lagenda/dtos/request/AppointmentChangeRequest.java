package com.lasystems.lagenda.dtos.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record AppointmentChangeRequest(
        @NotNull(message = "Id do agendamento é obrigatório")
        String appointmentId,
        @NotNull(message = "Data e hora do agendamento é obrigatório")
        LocalDateTime start,
        @NotNull(message = "SpecialistId é obrigatório")
        String specialistId,
        @NotNull(message = "ServiceId é obrigatório")
        List<String> serviceIds
) {
}
