package com.lasystems.lagenda.dtos.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public record AppointmentRequest(
        @NotNull(message = "ClientId é obrigatório")
        String clientId,
        @NotNull(message = "CompanyId é obrigatório")
        String companyId,
        LocalDateTime start,
        @NotNull(message = "ServiceId é obrigatório")
        List<String> serviceIds,
        @NotNull(message = "SpecialistId é obrigatório")
        String specialistId,
        String providerId,
        ZoneId companyZoneId

) {

}
