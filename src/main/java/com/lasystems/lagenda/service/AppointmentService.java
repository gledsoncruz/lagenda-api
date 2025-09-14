package com.lasystems.lagenda.service;

import com.lasystems.lagenda.dtos.SchedulingResult;
import com.lasystems.lagenda.dtos.appointments.AvailableTimesDto;
import com.lasystems.lagenda.dtos.request.AppointmentRequest;
import com.lasystems.lagenda.exceptions.EntityNotFoundException;
import com.lasystems.lagenda.models.Appointment;
import com.lasystems.lagenda.models.BusinessHour;
import com.lasystems.lagenda.models.Provider;
import com.lasystems.lagenda.repository.AppointmentRepository;
import com.lasystems.lagenda.repository.ProviderRepository;
import com.lasystems.lagenda.repository.ServiceRepository;
import com.lasystems.lagenda.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final SpecialtyRepository specialtyRepository;
    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;
    private final AppointmentRepository appointmentRepository;
    private final BusinessHourService businessHourService;
    private final CompanyClosureService companyClosureService;
    private static final int MAX_DAYS_AHEAD = 5;
    private static final int SLOT_MINUTES = 60; // duração padrão do serviço


    public void save(Appointment appointment) {
        appointmentRepository.save(appointment);
    }

    public Optional<SchedulingResult> findBestSlot(AppointmentRequest request) {
        if (request.companyId() == null || request.serviceIds() == null || request.serviceIds().isEmpty()) {
            return Optional.empty();
        }

        // 1. Obter especialidade (informada ou inferida)
        UUID specialtyId = getSpecialtyId(request);
        if (specialtyId == null || !specialtyRepository.existsByIdAndCompany(specialtyId, UUID.fromString(request.companyId()))) {
            return Optional.empty();
        }

        // 2. Calcular duração total
        Integer totalDuration = serviceRepository.getTotalDuration(request.serviceIds().stream().map(UUID::fromString).toList());
        if (totalDuration == null) totalDuration = SLOT_MINUTES; // padrão
        LocalDateTime end = request.start() != null ? request.start().plusMinutes(totalDuration) : null;

        // 3. Escolher cenário
        if (request.start() != null && request.providerId() != null) {
            return findWithDateTimeAndProvider(request, specialtyId, totalDuration);
        } else if (request.start() != null && request.providerId() == null) {
            return findWithDateTimeNoProvider(request, specialtyId, totalDuration);
        } else if (request.start() == null && request.providerId() != null) {
            return findWithNoDateTimeWithProvider(request, specialtyId, totalDuration);
        } else {
            return findWithNoDateTimeNoProvider(request, specialtyId, totalDuration);
        }
    }

    private UUID getSpecialtyId(AppointmentRequest request) {
        if (request.specialistId() != null) return UUID.fromString(request.specialistId());
        return serviceRepository.findCommonSpecialtyForServices(request.serviceIds().stream().map(UUID::fromString).toList(), UUID.fromString(request.companyId()));
    }

    private Optional<SchedulingResult> findWithDateTimeAndProvider(AppointmentRequest request, UUID specialtyId, int duration) {
        LocalDateTime start = request.start().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDateTime();
        LocalDateTime end = start.plusMinutes(duration);

        if (isAvailable(UUID.fromString(request.companyId()), UUID.fromString(request.providerId()), start, end, specialtyId)) {
            return Optional.of(new SchedulingResult(UUID.fromString(request.providerId()), start));
        }

        return findNextAvailableTimeForProvider(UUID.fromString(request.companyId()), UUID.fromString(request.providerId()), specialtyId, start.toLocalDate(), duration);
    }

    public Optional<SchedulingResult> findWithDateTimeNoProvider(AppointmentRequest request, UUID specialtyId, int duration) {
        LocalDateTime start = request.start();
        LocalDateTime end = start.plusMinutes(duration);

        List<Provider> available = providerRepository.findAvailableProviders(
                UUID.fromString(request.companyId()), specialtyId, start, end);

        if (available.isEmpty()) return Optional.empty();

        Provider best = available.stream()
                .min(Comparator.comparingInt(p -> getAppointmentCount(p.getId(), start)))
                .orElse(available.getFirst());

        return Optional.of(new SchedulingResult(best.getId(), start));
    }

    private Optional<SchedulingResult> findWithNoDateTimeWithProvider(AppointmentRequest request, UUID specialtyId, int duration) {
        return findNextAvailableTimeForProvider(UUID.fromString(request.companyId()), UUID.fromString(request.providerId()), specialtyId, LocalDate.now(), duration);
    }

    private Optional<SchedulingResult> findWithNoDateTimeNoProvider(AppointmentRequest request, UUID specialtyId, int duration) {
        LocalDate date = LocalDate.now();
        while (date.isBefore(date.plusWeeks(4))) {
            List<Provider> available = providerRepository.findProvidersBySpecialty(UUID.fromString(request.companyId()), specialtyId);
            for (LocalTime time : getAvailableTimesOfDay(UUID.fromString(request.companyId()), date)) {
                LocalDateTime start = date.atTime(time);
                LocalDateTime end = start.plusMinutes(duration);
                for (Provider p : available) {
                    if (isAvailable(UUID.fromString(request.companyId()), p.getId(), start, end, specialtyId)) {
                        return Optional.of(new SchedulingResult(p.getId(), start));
                    }
                }
            }
            date = date.plusDays(1);
        }
        return Optional.empty();
    }

    private Optional<SchedulingResult> findNextAvailableTimeForProvider(UUID companyId, UUID providerId, UUID specialtyId, LocalDate date, int duration) {
        if (date.isBefore(LocalDate.now())) {
            return Optional.empty();
        }
        LocalDateTime current = date.atStartOfDay().plusHours(9);
        LocalDateTime currentWithZoneId = current.atZone(ZoneId.of("America/Sao_Paulo")).toLocalDateTime();
        while (currentWithZoneId.toLocalTime().isAfter(LocalTime.of(8, 59)) &&
                currentWithZoneId.toLocalTime().isBefore(LocalTime.of(18, 0))) {
            LocalDateTime end = currentWithZoneId.plusMinutes(duration);
            if (isAvailable(companyId, providerId, currentWithZoneId, end, specialtyId)) {
                return Optional.of(new SchedulingResult(providerId, currentWithZoneId));
            }
            currentWithZoneId = currentWithZoneId.plusHours(1);
        }
        return Optional.empty();
    }

    private List<LocalTime> getAvailableTimesOfDay(UUID companyId, LocalDate date) {
        // Retorne os horários comerciais em blocos de 1 hora
        return List.of(
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                LocalTime.of(17, 0)
        );
    }

    public boolean isAvailable(UUID companyId, UUID providerId, LocalDateTime start, LocalDateTime end, UUID specialtyId) {
        if (start.isBefore(LocalDateTime.now())) {
            return false;
        }
        boolean inBusinessHours = businessHourService.isWithinBusinessHours(companyId, start, end);
        boolean notClosed = companyClosureService.isAvailableForScheduling(companyId, start, end);
        boolean noConflict = !appointmentRepository.hasOverlappingAppointment(companyId, providerId, start, end);
        boolean hasSpecialty = providerRepository.findProvidersBySpecialty(companyId, specialtyId).stream()
                .anyMatch(p -> p.getId().equals(providerId));

        return inBusinessHours && notClosed && noConflict && hasSpecialty;
    }

    public int getAppointmentCount(UUID providerId, LocalDateTime start) {
        LocalDate date = start.toLocalDate();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime nextStartOfDay = startOfDay.plusDays(1);

        return appointmentRepository.countAppointmentsByProviderAndDate(
                providerId, startOfDay, nextStartOfDay
        );
    }


    public Appointment findById(String id) {
        return appointmentRepository.findById(UUID.fromString(id)).orElseThrow(EntityNotFoundException::new);

    }

    public Appointment update(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> pastAppointmentsScheduled() {
        return appointmentRepository.pastAppointmentsScheduled();
    }

    public void cancelAppointmentsBatch(List<UUID> ids) {
        appointmentRepository.cancelAppointmentsBatch(ids);
    }

    /**
     * Encontra o próximo dia com horários disponíveis, até 5 dias à frente.
     * Retorna o primeiro dia com pelo menos um horário livre.
     */
    public Optional<AvailableTimesDto> findNextAvailableTimes(
            UUID companyId,
            List<UUID> serviceIds,
            LocalDate targetDate
    ) {
        // 1. Calcular duração total dos serviços
        int durationMinutes = serviceRepository.getTotalDuration(serviceIds);

        // 2. Buscar a partir do dia desejado
        LocalDate date = targetDate;

        for (int i = 0; i < MAX_DAYS_AHEAD; i++) {
            List<LocalTime> available = getAvailableTimesForDay(companyId, date, durationMinutes);
            if (!available.isEmpty()) {
                return Optional.of(new AvailableTimesDto(date, available));
            }
            date = date.plusDays(1);
        }

        return Optional.empty();
    }

    /**
     * Retorna os horários disponíveis em um dia específico.
     */
    private List<LocalTime> getAvailableTimesForDay(UUID companyId, LocalDate date, int durationMinutes) {
        // 1. Obter os horários comerciais do dia
        Integer dayOfWeek = businessHourService.toDatabaseDayOfWeek(date.getDayOfWeek());
        List<BusinessHour> businessHours = businessHourService.findByCompanyIdAndDayOfWeek(companyId, dayOfWeek);

        if (businessHours.isEmpty()) {
            return List.of();
        }

        // 2. Gerar slots disponíveis dentro de cada bloco
        List<LocalTime> availableSlots = new ArrayList<>();

        for (BusinessHour bh : businessHours) {
            LocalTime start = bh.getStartTime();
            LocalTime end = bh.getEndTime();

            // Gerar slots de 60 min dentro do bloco, respeitando a duração
            LocalTime slotTime = start;
            while (slotTime.plusMinutes(durationMinutes).isBefore(end) ||
                    slotTime.plusMinutes(durationMinutes).equals(end)) {
                availableSlots.add(slotTime);
                slotTime = slotTime.plusMinutes(SLOT_MINUTES);
            }
        }

        // 3. Filtrar horários com conflito de agendamento ou fechamento
        return availableSlots.stream()
                .filter(time -> {
                    LocalDateTime slotStart = date.atTime(time);
                    LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

                    // Verifica fechamento
                    if (!companyClosureService.isAvailableForScheduling(companyId, slotStart, slotEnd)) {
                        return false;
                    }

                    // Verifica agendamento conflitante
                    return !hasOverlappingAppointment(companyId, slotStart, slotEnd);
                })
                .sorted()
                .toList();
    }

    /**
     * Obtém os horários comerciais em blocos de 15 min (ou intervalo desejado)
     */
    private List<LocalTime> getBusinessHoursOn(LocalDate date, UUID companyId) {
//        DayOfWeek dayOfWeek = date.getDayOfWeek().getValue() % 7; // 0 = Domingo
        Integer dayOfWeek = businessHourService.toDatabaseDayOfWeek(date.getDayOfWeek());
        List<BusinessHour> hours = businessHourService.findByCompanyIdAndDayOfWeek(companyId, dayOfWeek);

        return hours.stream()
                .flatMap(bh -> IntStream.iterate(
                                bh.getStartTime().toSecondOfDay() / 60,
                                t -> t + 60)
                        .limit((bh.getEndTime().toSecondOfDay() - bh.getStartTime().toSecondOfDay()) / 60 / 60 + 1)
                        .mapToObj(min -> LocalTime.ofSecondOfDay(min * 60L)))
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Verifica se há agendamento conflitante
     */
    private boolean hasOverlappingAppointment(UUID companyId, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.hasOverlappingAppointment(companyId, null, start, end);
    }

    public boolean isAvailableForClient(UUID clientId, LocalDateTime start, LocalDateTime end) {
        return !appointmentRepository.hasOverlappingAppointmentForClient(clientId, start, end);
    }

//    public List<Appointment> updateStatus(List<Appointment> appointments) {
//        return appointmentRepository.saveAll(appointments);
//    }

}
