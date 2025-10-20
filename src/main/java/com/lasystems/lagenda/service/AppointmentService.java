package com.lasystems.lagenda.service;

import com.lasystems.lagenda.constants.AppointmentsConstants;
import com.lasystems.lagenda.dtos.ProviderMinAppointmentsDto;
import com.lasystems.lagenda.dtos.SchedulingResult;
import com.lasystems.lagenda.dtos.appointments.AvailableTimesDto;
import com.lasystems.lagenda.dtos.request.AppointmentChangeRequest;
import com.lasystems.lagenda.dtos.request.AppointmentRequest;
import com.lasystems.lagenda.exceptions.EntityNotFoundException;
import com.lasystems.lagenda.exceptions.NoProviderAvailableException;
import com.lasystems.lagenda.models.*;
import com.lasystems.lagenda.models.enums.AppointmentStatus;
import com.lasystems.lagenda.repository.AppointmentRepository;
import com.lasystems.lagenda.repository.ProviderRepository;
import com.lasystems.lagenda.repository.ServiceRepository;
import com.lasystems.lagenda.util.ConvertAndFormatUtil;
import com.lasystems.lagenda.validators.UUIDValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsável pela lógica de negócio de agendamentos.
 * Refatorado para melhor separação de responsabilidades e manutenibilidade.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentValidationService validationService;
    private final ProviderRepository providerRepository;
    private final ServiceRepository serviceRepository;
    private final ClientService clientService;
    private final ProviderService providerService;
    private final ServiceService serviceService;
    private final BusinessHourService businessHourService;
    private final N8nIntegrationService n8nIntegrationService;

    /**
     * Cria um novo agendamento.
     */
    @Transactional
    public Appointment createAppointment(AppointmentRequest request) {
        log.info("Iniciando criação de agendamento para cliente {}", request.clientId());

        // 1. Resolver prestador (fornecido ou automático)
        Provider provider = resolveProvider(request);

        // 2. Buscar serviços e cliente
        List<com.lasystems.lagenda.models.Service> services =
                serviceService.findAllById(request.serviceIds());
        int durationMinutes = serviceRepository.getTotalDuration(UUIDValidator.parseList(request.serviceIds(), "serviceIds"));
        Client client = clientService.findById(request.clientId());

        // 3. Calcular período do agendamento
        LocalDateTime start = request.start();
        LocalDateTime end = start.plusMinutes(durationMinutes);
        UUID specialtyId = UUIDValidator.parseOrThrow(request.specialistId(), "specialistId");

        // 4. Validar disponibilidade
        validationService.validateSlotAvailability(
                UUID.fromString(request.companyId()),
                provider.getId(),
                start,
                end,
                specialtyId
        );

        validationService.validateClientAvailability(client.getId(), start, end);

        // 5. Criar agendamento
        Appointment appointment = buildAppointment(client, provider, services, start, end);
        Appointment saved = save(appointment);

        // 6. Notificar sistemas externos
        notifyExternalSystems(saved, AppointmentsConstants.GoogleCalendarOperation.CREATE_EVENT);

        log.info("Agendamento {} criado com sucesso", saved.getId());
        return saved;
    }

    /**
     * Altera um agendamento existente.
     */
    @Transactional
    public Appointment changeAppointment(AppointmentChangeRequest request) {
        log.info("Alterando agendamento {}", request.appointmentId());

        Appointment appointment = findById(request.appointmentId());
        List<com.lasystems.lagenda.models.Service> services =
                serviceService.findAllById(request.serviceIds());
        int durationMinutes = serviceRepository.getTotalDuration(UUIDValidator.parseList(request.serviceIds(), "serviceIds"));

        LocalDateTime newStart = request.start();
        LocalDateTime newEnd = newStart.plusMinutes(durationMinutes);
        UUID specialtyId = UUIDValidator.parseOrThrow(request.specialistId(), "specialistId");

        // Validar disponibilidade apenas se horário mudou
        if (!validationService.isSameTimeSlot(appointment.getStart(), newStart)) {
            validationService.validateSlotAvailability(
                    appointment.getCompany().getId(),
                    appointment.getProvider().getId(),
                    newStart,
                    newEnd,
                    specialtyId
            );
        }

        // Atualizar dados
        updateAppointmentDetails(appointment, services, newStart, newEnd);
        Appointment updated = save(appointment);

        // Notificar atualização
        notifyExternalSystems(updated, AppointmentsConstants.GoogleCalendarOperation.UPDATE_EVENT);

        log.info("Agendamento {} alterado com sucesso", updated.getId());
        return updated;
    }

    /**
     * Altera o status de um agendamento.
     */
    @Transactional
    public Appointment changeStatus(String appointmentId, AppointmentStatus newStatus) {
        log.info("Alterando status do agendamento {} para {}", appointmentId, newStatus);

        Appointment appointment = findById(appointmentId);
        appointment.setStatus(newStatus);
        Appointment updated = save(appointment);

        if (newStatus == AppointmentStatus.CANCELLED) {
            notifyExternalSystems(updated, AppointmentsConstants.GoogleCalendarOperation.CANCEL_EVENT);
        }

        log.info("Status alterado com sucesso");
        return updated;
    }

    /**
     * Finaliza agendamentos passados que ainda estão como SCHEDULED.
     */
    @Transactional
    public List<Appointment> finalizeMissedAppointments() {
        log.info("Finalizando agendamentos perdidos");

        List<Appointment> pastAppointments = appointmentRepository.pastAppointmentsScheduled();

        if (pastAppointments.isEmpty()) {
            log.info("Nenhum agendamento perdido encontrado");
            return List.of();
        }

        List<UUID> ids = pastAppointments.stream()
                .map(Appointment::getId)
                .toList();

        appointmentRepository.cancelAppointmentsBatch(ids);

        log.info("Total de {} agendamentos finalizados", ids.size());
        return pastAppointments;
    }

    /**
     * Busca o próximo dia com horários disponíveis.
     */
    @Transactional(readOnly = true)
    public Optional<AvailableTimesDto> findNextAvailableTimes(
            UUID companyId,
            List<UUID> serviceIds,
            LocalDate targetDate
    ) {
        log.debug("Buscando próximos horários disponíveis para company={}", companyId);

        int durationMinutes = serviceRepository.getTotalDuration(serviceIds);
        LocalDate date = targetDate;

        for (int i = 0; i < AppointmentsConstants.MAX_SEARCH_DAYS_AHEAD; i++) {
            List<LocalTime> available = getAvailableTimesForDay(companyId, date, durationMinutes);
            if (!available.isEmpty()) {
                return Optional.of(new AvailableTimesDto(date, available));
            }
            date = date.plusDays(1);
        }

        return Optional.empty();
    }

    /**
     * Encontra o melhor slot disponível baseado nos critérios da requisição.
     */
    @Transactional(readOnly = true)
    public Optional<SchedulingResult> findBestSlot(AppointmentRequest request) {
        log.debug("Buscando melhor slot para request: {}", request);

//        UUID companyId = UUIDValidator.parseOrThrow(request.companyId(), "companyId");
        UUID specialtyId = getOrInferSpecialtyId(request);

        Integer totalDuration = serviceRepository.getTotalDuration(
                UUIDValidator.parseList(request.serviceIds(), "serviceIds")
        );
        if (totalDuration == null) {
            totalDuration = AppointmentsConstants.DEFAULT_SLOT_DURATION_MINUTES;
        }

        // Determinar cenário e buscar slot apropriado
        if (request.start() != null && request.providerId() != null) {
            return findBestSlotWithDateTimeAndProvider(request, specialtyId, totalDuration);
        } else if (request.start() != null) {
            return findBestSlotWithDateTime(request, specialtyId, totalDuration);
        } else if (request.providerId() != null) {
            return findBestSlotWithProvider(request, specialtyId, totalDuration);
        } else {
            return findBestSlotAutomatic(request, specialtyId, totalDuration);
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Resolve qual prestador usar: fornecido ou automático (com menos agendamentos).
     */
    private Provider resolveProvider(AppointmentRequest request) {
        if (request.providerId() != null && !request.providerId().isEmpty()) {
            return providerService.findById(request.providerId());
        }

        LocalDate appointmentDate = request.start().toLocalDate();
        Optional<ProviderMinAppointmentsDto> bestProvider =
                providerService.findProviderWithLeastAppointments(
                        request.companyId(),
                        request.specialistId(),
                        appointmentDate
                );

        if (bestProvider.isEmpty()) {
            throw new NoProviderAvailableException(
                    UUIDValidator.parseOrThrow(request.specialistId(), "specialistId")
            );
        }

        return providerService.findById(bestProvider.get().getId().toString());
    }

    /**
     * Constrói um objeto Appointment com os dados fornecidos.
     */
    private Appointment buildAppointment(
            Client client,
            Provider provider,
            List<com.lasystems.lagenda.models.Service> services,
            LocalDateTime start,
            LocalDateTime end
    ) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String notes = String.format(
                "Nome: %s\nData do Agendamento: %s\nHorário: %s\nServiço(s): %s\nTotal: %s",
                client.getName(),
                start.format(dateFormatter),
                start.format(timeFormatter),
                services.stream().map(com.lasystems.lagenda.models.Service::getName)
                        .collect(Collectors.joining(", ")),
                ConvertAndFormatUtil.calcularValorTotalFormatado(services)
        );

        Appointment appointment = Appointment.builder()
                .company(client.getCompany())
                .client(client)
                .provider(provider)
                .start(start)
                .end(end)
                .notes(notes)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        services.forEach(appointment::addService);

        return appointment;
    }

    /**
     * Atualiza detalhes de um agendamento existente.
     */
    private void updateAppointmentDetails(
            Appointment appointment,
            List<com.lasystems.lagenda.models.Service> services,
            LocalDateTime start,
            LocalDateTime end
    ) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String notes = String.format(
                "Nome: %s\nData do Agendamento: %s\nHorário: %s\nServiço(s): %s\nTotal: %s",
                appointment.getClient().getName(),
                start.format(dateFormatter),
                start.format(timeFormatter),
                services.stream().map(com.lasystems.lagenda.models.Service::getName)
                        .collect(Collectors.joining(", ")),
                ConvertAndFormatUtil.calcularValorTotalFormatado(services)
        );

        appointment.setStart(start);
        appointment.setEnd(end);
        appointment.setNotes(notes);
        appointment.updateServices(services);
    }

    /**
     * Salva um agendamento no banco de dados.
     */
    private Appointment save(Appointment appointment) {
        try {
            return appointmentRepository.save(appointment);
        } catch (Exception e) {
            log.error("Erro ao salvar agendamento: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar agendamento", e);
        }
    }

    /**
     * Notifica sistemas externos (Google Calendar via N8N).
     */
    private void notifyExternalSystems(Appointment appointment, int operation) {
        try {
            n8nIntegrationService.notifyGoogleCalendarN8N(appointment, operation);
        } catch (Exception e) {
            log.error("Erro ao notificar sistemas externos: {}", e.getMessage(), e);
            // Não propagar erro para não falhar o agendamento principal
        }
    }

    /**
     * Retorna horários disponíveis em um dia específico.
     */
    private List<LocalTime> getAvailableTimesForDay(
            UUID companyId,
            LocalDate date,
            int durationMinutes
    ) {
        Integer dayOfWeek = businessHourService.toDatabaseDayOfWeek(date.getDayOfWeek());
        List<BusinessHour> businessHours =
                businessHourService.findByCompanyIdAndDayOfWeek(companyId, dayOfWeek);

        if (businessHours.isEmpty()) {
            return List.of();
        }

        List<LocalTime> slots = new ArrayList<>();

        for (BusinessHour bh : businessHours) {
            LocalTime slotTime = bh.getStartTime();

            while (slotTime.plusMinutes(durationMinutes).isBefore(bh.getEndTime()) ||
                    slotTime.plusMinutes(durationMinutes).equals(bh.getEndTime())) {
                slots.add(slotTime);
                slotTime = slotTime.plusMinutes(AppointmentsConstants.SLOT_INTERVAL_MINUTES);
            }
        }

        return slots.stream()
                .filter(time -> isTimeSlotAvailable(companyId, date, time, durationMinutes))
                .sorted()
                .toList();
    }

    /**
     * Verifica se um slot de tempo específico está disponível.
     */
    private boolean isTimeSlotAvailable(
            UUID companyId,
            LocalDate date,
            LocalTime time,
            int durationMinutes
    ) {
        LocalDateTime start = date.atTime(time);
        LocalDateTime end = start.plusMinutes(durationMinutes);

        return !appointmentRepository.hasOverlappingAppointment(companyId, null, start, end);
    }

    /**
     * Obtém ou infere o ID da especialidade.
     */
    private UUID getOrInferSpecialtyId(AppointmentRequest request) {
        if (request.specialistId() != null) {
            return UUIDValidator.parseOrThrow(request.specialistId(), "specialistId");
        }

        UUID specialtyId = serviceRepository.findCommonSpecialtyForServices(
                UUIDValidator.parseList(request.serviceIds(), "serviceIds"),
                UUIDValidator.parseOrThrow(request.companyId(), "companyId")
        );

        if (specialtyId == null) {
            throw new IllegalArgumentException("Não foi possível determinar a especialidade");
        }

        return specialtyId;
    }

    /**
     * Busca melhor slot quando data/hora e prestador são fornecidos.
     */
    private Optional<SchedulingResult> findBestSlotWithDateTimeAndProvider(
            AppointmentRequest request,
            UUID specialtyId,
            int duration
    ) {
        LocalDateTime start = request.start();
        LocalDateTime end = start.plusMinutes(duration);
        UUID companyId = UUIDValidator.parseOrThrow(request.companyId(), "companyId");
        UUID providerId = UUIDValidator.parseOrThrow(request.providerId(), "providerId");

        try {
            validationService.validateSlotAvailability(companyId, providerId, start, end, specialtyId);
            return Optional.of(new SchedulingResult(providerId, start));
        } catch (Exception e) {
            log.debug("Slot não disponível, buscando próximo: {}", e.getMessage());
            return findNextAvailableForProvider(companyId, providerId, specialtyId, start.toLocalDate(), duration);
        }
    }

    /**
     * Busca melhor slot quando apenas data/hora é fornecida.
     */
    private Optional<SchedulingResult> findBestSlotWithDateTime(
            AppointmentRequest request,
            UUID specialtyId,
            int duration
    ) {
        LocalDateTime start = request.start();
        LocalDateTime end = start.plusMinutes(duration);
        UUID companyId = UUIDValidator.parseOrThrow(request.companyId(), "companyId");

        List<Provider> availableProviders = providerRepository.findAvailableProviders(
                companyId, specialtyId, start, end
        );

        if (availableProviders.isEmpty()) {
            return Optional.empty();
        }

        Provider bestProvider = availableProviders.stream()
                .min(Comparator.comparingInt(p -> countAppointmentsOnDate(p.getId(), start)))
                .orElse(availableProviders.get(0));

        return Optional.of(new SchedulingResult(bestProvider.getId(), start));
    }

    /**
     * Busca melhor slot quando apenas prestador é fornecido.
     */
    private Optional<SchedulingResult> findBestSlotWithProvider(
            AppointmentRequest request,
            UUID specialtyId,
            int duration
    ) {
        UUID companyId = UUIDValidator.parseOrThrow(request.companyId(), "companyId");
        UUID providerId = UUIDValidator.parseOrThrow(request.providerId(), "providerId");

        return findNextAvailableForProvider(companyId, providerId, specialtyId, LocalDate.now(), duration);
    }

    /**
     * Busca melhor slot automaticamente (sem preferências).
     */
    private Optional<SchedulingResult> findBestSlotAutomatic(
            AppointmentRequest request,
            UUID specialtyId,
            int duration
    ) {
        UUID companyId = UUIDValidator.parseOrThrow(request.companyId(), "companyId");
        LocalDate searchDate = LocalDate.now();
        LocalDate maxDate = searchDate.plusWeeks(4);

        while (searchDate.isBefore(maxDate)) {
            List<Provider> providers = providerRepository.findProvidersBySpecialty(companyId, specialtyId);

            for (Provider provider : providers) {
                Optional<SchedulingResult> result = findFirstAvailableSlotForProviderOnDate(
                        companyId, provider.getId(), specialtyId, searchDate, duration
                );

                if (result.isPresent()) {
                    return result;
                }
            }

            searchDate = searchDate.plusDays(1);
        }

        return Optional.empty();
    }

    /**
     * Encontra o próximo horário disponível para um prestador específico.
     */
    private Optional<SchedulingResult> findNextAvailableForProvider(
            UUID companyId,
            UUID providerId,
            UUID specialtyId,
            LocalDate startDate,
            int duration
    ) {
        if (startDate.isBefore(LocalDate.now())) {
            startDate = LocalDate.now();
        }

        LocalDate maxDate = startDate.plusWeeks(4);

        while (startDate.isBefore(maxDate)) {
            Optional<SchedulingResult> result = findFirstAvailableSlotForProviderOnDate(
                    companyId, providerId, specialtyId, startDate, duration
            );

            if (result.isPresent()) {
                return result;
            }

            startDate = startDate.plusDays(1);
        }

        return Optional.empty();
    }

    /**
     * Encontra o primeiro slot disponível para um prestador em uma data específica.
     */
    private Optional<SchedulingResult> findFirstAvailableSlotForProviderOnDate(
            UUID companyId,
            UUID providerId,
            UUID specialtyId,
            LocalDate date,
            int duration
    ) {
        List<LocalTime> availableTimes = getAvailableTimesForDay(companyId, date, duration);

        for (LocalTime time : availableTimes) {
            LocalDateTime start = date.atTime(time);
            LocalDateTime end = start.plusMinutes(duration);

            try {
                validationService.validateSlotAvailability(companyId, providerId, start, end, specialtyId);
                return Optional.of(new SchedulingResult(providerId, start));
            } catch (Exception e) {
                // Tentar próximo horário
                continue;
            }
        }

        return Optional.empty();
    }

    /**
     * Conta agendamentos de um prestador em uma data específica.
     */
    private int countAppointmentsOnDate(UUID providerId, LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime nextDay = startOfDay.plusDays(1);

        return appointmentRepository.countAppointmentsByProviderAndDate(
                providerId, startOfDay, nextDay
        );
    }

    // ==================== MÉTODOS PÚBLICOS AUXILIARES ====================

    public Appointment findById(String id) {
        UUID uuid = com.lasystems.lagenda.validators.UUIDValidator.parseOrThrow(id, "appointmentId");
        return appointmentRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException(Appointment.class, uuid));
    }

    public boolean isAvailable(
            UUID companyId,
            UUID providerId,
            LocalDateTime start,
            LocalDateTime end,
            UUID specialtyId
    ) {
        try {
            validationService.validateSlotAvailability(companyId, providerId, start, end, specialtyId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAvailableForClient(UUID clientId, LocalDateTime start, LocalDateTime end) {
        try {
            validationService.validateClientAvailability(clientId, start, end);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}