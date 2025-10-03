package com.lasystems.lagenda.controllers;

import com.lasystems.lagenda.constants.AppointmentsConstants;
import com.lasystems.lagenda.dtos.SchedulingResult;
import com.lasystems.lagenda.dtos.appointments.AvailableTimesDto;
import com.lasystems.lagenda.dtos.request.AppointmentChangeRequest;
import com.lasystems.lagenda.dtos.request.AppointmentChangeStatusRequest;
import com.lasystems.lagenda.dtos.request.AppointmentRequest;
import com.lasystems.lagenda.dtos.*;
import com.lasystems.lagenda.models.Appointment;
import com.lasystems.lagenda.models.enums.AppointmentStatus;
import com.lasystems.lagenda.service.AppointmentService;
import com.lasystems.lagenda.validators.UUIDValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de agendamentos.
 * Refatorado para usar DTOs de response tipados e delegar lógica ao Service.
 */
@Slf4j
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Cria um novo agendamento.
     *
     * @param request dados do agendamento
     * @return resposta com informações do agendamento criado
     */
    @PostMapping("/create")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestBody @Valid AppointmentRequest request
    ) {
        log.info("Requisição de criação de agendamento recebida: clientId={}", request.clientId());

        Appointment appointment = appointmentService.createAppointment(request);

        AppointmentResponse response = new AppointmentResponse(
                AppointmentsConstants.Messages.APPOINTMENT_CREATED,
                appointment.getId(),
                appointment.getStart()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Altera um agendamento existente.
     *
     * @param request dados da alteração
     * @return resposta com informações do agendamento alterado
     */
    @PutMapping("/change-appointment")
    public ResponseEntity<AppointmentResponse> changeAppointment(
            @RequestBody @Valid AppointmentChangeRequest request
    ) {
        log.info("Requisição de alteração de agendamento: id={}", request.appointmentId());

        Appointment appointment = appointmentService.changeAppointment(request);

        AppointmentResponse response = new AppointmentResponse(
                AppointmentsConstants.Messages.APPOINTMENT_UPDATED,
                appointment.getId(),
                appointment.getStart()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Altera o status de um agendamento.
     *
     * @param request dados da alteração de status
     * @return resposta confirmando alteração
     */
    @PutMapping("/change-status")
    public ResponseEntity<StatusChangeResponse> changeStatus(
            @RequestBody @Valid AppointmentChangeStatusRequest request
    ) {
        log.info("Requisição de mudança de status: id={}, status={}",
                request.appointmentId(), request.appointmentStatus());

        AppointmentStatus newStatus = AppointmentStatus.valueOf(request.appointmentStatus());
        Appointment appointment = appointmentService.changeStatus(
                request.appointmentId(),
                newStatus
        );

        StatusChangeResponse response = new StatusChangeResponse(
                AppointmentsConstants.Messages.STATUS_CHANGED,
                appointment.getId(),
                appointment.getStatus().name()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Verifica disponibilidade de um horário.
     *
     * @param request dados para verificação
     * @return resposta indicando se está disponível
     */
    @GetMapping("/check-availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @RequestBody @Valid AppointmentRequest request
    ) {
        log.debug("Verificando disponibilidade: {}", request.start());

        UUID companyId = UUIDValidator.parseOrThrow(request.companyId(), "companyId");
        UUID specialtyId = UUIDValidator.parseOrThrow(request.specialistId(), "specialistId");
        UUID providerId = request.providerId() != null
                ? UUIDValidator.parseOrThrow(request.providerId(), "providerId")
                : null;

        boolean available = appointmentService.isAvailable(
                companyId,
                providerId,
                request.start(),
                request.start().plusHours(1),
                specialtyId
        );

        String message = available
                ? "Horário disponível para agendamento"
                : AppointmentsConstants.Messages.SLOT_NOT_AVAILABLE;

        AvailabilityResponse response = new AvailabilityResponse(
                message,
                available,
                request.start()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Encontra o melhor slot disponível.
     *
     * @param request critérios de busca
     * @return melhor slot encontrado ou 404
     */
    @GetMapping("/find-best-slot")
    public ResponseEntity<BestSlotResponse> findBestSlot(
            @RequestBody @Valid AppointmentRequest request
    ) {
        log.info("Buscando melhor slot disponível");

        return appointmentService.findBestSlot(request)
                .map(slot -> {
                    BestSlotResponse response = new BestSlotResponse(
                            slot.providerId(),
                            "Provider Name", // TODO: Buscar nome do provider
                            slot.startTime()
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca próximos horários disponíveis.
     *
     * @param request critérios de busca
     * @return próximos horários disponíveis ou 404
     */
    @GetMapping("/next-available")
    public ResponseEntity<NextAvailableTimesResponse> getNextAvailableTimes(
            @RequestBody @Valid AppointmentRequest request
    ) {
        log.info("Buscando próximos horários disponíveis");
        if (request.start().isBefore(LocalDateTime.now())) {
            return ResponseEntity.notFound().build();
        }

        UUID companyId = UUIDValidator.parseOrThrow(request.companyId(), "companyId");
        List<UUID> serviceIds = UUIDValidator.parseList(request.serviceIds(), "serviceIds");

        return appointmentService.findNextAvailableTimes(
                        companyId,
                        serviceIds,
                        request.start().toLocalDate()
                )
                .map(dto -> {
                    List<String> formattedTimes = dto.availableTimes().stream()
                            .map(time -> time.format(DateTimeFormatter.ofPattern("HH:mm")))
                            .toList();

                    NextAvailableTimesResponse response = new NextAvailableTimesResponse(
                            dto.date().toString(),
                            formattedTimes
                    );

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Finaliza agendamentos passados que ainda estão como SCHEDULED.
     *
     * @return resposta com total de agendamentos atualizados
     */
    @PostMapping("/finalize-missed")
    public ResponseEntity<FinalizeMissedAppointmentsResponse> finalizeMissedAppointments() {
        log.info("Finalizando agendamentos perdidos");

        List<Appointment> finalized = appointmentService.finalizeMissedAppointments();

        FinalizeMissedAppointmentsResponse response = new FinalizeMissedAppointmentsResponse(
                String.format("Total de %d agendamentos finalizados", finalized.size()),
                finalized.size(),
                finalized.stream().map(Appointment::getId).toList()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Busca detalhes de um agendamento por ID.
     *
     * @param id ID do agendamento
     * @return detalhes do agendamento
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDetailResponse> getAppointmentById(
            @PathVariable String id
    ) {
        log.debug("Buscando agendamento por ID: {}", id);

        Appointment appointment = appointmentService.findById(id);

        AppointmentDetailResponse response = new AppointmentDetailResponse(
                appointment.getId(),
                new ClientSummary(
                        appointment.getClient().getId(),
                        appointment.getClient().getName(),
                        appointment.getClient().getPhone()
                ),
                new ProviderSummary(
                        appointment.getProvider().getId(),
                        appointment.getProvider().getName(),
                        appointment.getProvider().getCalendarId()
                ),
                appointment.getStart(),
                appointment.getEnd(),
                appointment.getStatus().name(),
                appointment.getServices().stream()
                        .map(s -> new ServiceSummary(
                                s.getId(),
                                s.getName(),
                                s.getPrice(),
                                s.getDurationMinutes()
                        ))
                        .collect(Collectors.toList()),
                appointment.getNotes()
        );

        return ResponseEntity.ok(response);
    }
}