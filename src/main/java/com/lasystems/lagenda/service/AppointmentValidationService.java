package com.lasystems.lagenda.service;

import com.lasystems.lagenda.exceptions.ClientConflictException;
import com.lasystems.lagenda.exceptions.SlotNotAvailableException;
import com.lasystems.lagenda.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service responsável por validações de agendamentos.
 * Centraliza toda lógica de validação para evitar duplicação.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentValidationService {

    private final AppointmentRepository appointmentRepository;
    private final BusinessHourService businessHourService;
    private final CompanyClosureService companyClosureService;

    /**
     * Valida se um horário está disponível para agendamento.
     *
     * @throws SlotNotAvailableException se não estiver disponível
     */
    public void validateSlotAvailability(
            UUID companyId,
            UUID providerId,
            LocalDateTime start,
            LocalDateTime end,
            UUID specialtyId
    ) {
        log.debug("Validando disponibilidade: company={}, provider={}, start={}",
                companyId, providerId, start);

        // 1. Validar se está no passado
        if (start.isBefore(LocalDateTime.now())) {
            throw new SlotNotAvailableException(start, "Não é possível agendar no passado");
        }

        // 2. Validar horário comercial
        if (!businessHourService.isWithinBusinessHours(companyId, start, end)) {
            throw new SlotNotAvailableException(start, "Fora do horário comercial");
        }

        // 3. Validar fechamentos
        if (!companyClosureService.isAvailableForScheduling(companyId, start, end)) {
            throw new SlotNotAvailableException(start, "Empresa fechada neste período");
        }

        // 4. Validar conflitos de agendamento
        if (appointmentRepository.hasOverlappingAppointment(companyId, providerId, start, end)) {
            throw new SlotNotAvailableException(start, "Horário já ocupado");
        }

        log.debug("Horário validado com sucesso");
    }

    /**
     * Valida se um cliente está disponível para agendamento.
     *
     * @throws ClientConflictException se o cliente já tiver agendamento
     */
    public void validateClientAvailability(
            UUID clientId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        log.debug("Validando disponibilidade do cliente: id={}, start={}", clientId, start);

        if (appointmentRepository.hasOverlappingAppointmentForClient(clientId, start, end)) {
            throw new ClientConflictException(clientId, start);
        }

        log.debug("Cliente disponível para agendamento");
    }

    /**
     * Valida se um período de agendamento é válido.
     */
    public void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Data de início e fim são obrigatórias");
        }

        if (end.isBefore(start) || end.equals(start)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível agendar no passado");
        }
    }

    /**
     * Valida se é uma atualização do mesmo horário (não precisa revalidar disponibilidade).
     */
    public boolean isSameTimeSlot(LocalDateTime existingStart, LocalDateTime newStart) {
        return existingStart.equals(newStart);
    }
}