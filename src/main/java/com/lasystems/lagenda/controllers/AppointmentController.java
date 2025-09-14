package com.lasystems.lagenda.controllers;


import com.lasystems.lagenda.dtos.ProviderMinAppointmentsDto;
import com.lasystems.lagenda.dtos.request.AppointmentChangeRequest;
import com.lasystems.lagenda.dtos.request.AppointmentChangeStatusRequest;
import com.lasystems.lagenda.dtos.request.AppointmentRequest;
import com.lasystems.lagenda.models.Appointment;
import com.lasystems.lagenda.models.Client;
import com.lasystems.lagenda.models.Provider;
import com.lasystems.lagenda.models.Service;
import com.lasystems.lagenda.models.enums.AppointmentStatus;
import com.lasystems.lagenda.service.AppointmentService;
import com.lasystems.lagenda.service.ClientService;
import com.lasystems.lagenda.service.ProviderService;
import com.lasystems.lagenda.service.ServiceService;
import com.lasystems.lagenda.util.ConvertAndFormatUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final ClientService clientService;
    private final ProviderService providerService;
    private final ServiceService serviceService;
    private final AppointmentService appointmentService;

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody @Valid AppointmentRequest request) {

        Provider provider = new Provider();
        LocalDate appointmentDate = request.start().toLocalDate();
        List<Service> serviceList = serviceService.findAllById(request.serviceIds());

        if (request.providerId() != null && !request.providerId().isEmpty()) {
            provider = providerService.findById(request.providerId());
        } else {

            Optional<ProviderMinAppointmentsDto> bestProvider =
                    providerService.findProviderWithLeastAppointments(request.companyId(),request.specialistId() , appointmentDate);

            if (bestProvider.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(Map.of(
                                "message", "Nenhum prestador disponível com essa especialidade."
                        ));
            }

            provider = providerService.findById(bestProvider.get().getId().toString());

        }

        if (appointmentService.isAvailable(UUID.fromString(request.companyId()), provider.getId(),
                request.start(), request.start().plusHours(1), UUID.fromString(request.specialistId()))) {

            Client client = clientService.findById(request.clientId());

            if (!appointmentService.isAvailableForClient(client.getId(), request.start(), request.start().plusHours(1))) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(Map.of(
                                "message", "Cliente já possui agendamento nesse mesmo dia e horario escolhido."
                        ));
            }

            DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
            String dataFormatada = request.start().format(formatoData);
            String horaFormatada = request.start().format(formatoHora);

            Appointment appointment = Appointment.builder()
                    .notes(String.format("Nome: %s\nData do Agendamento: %s\nHorário: %s\nServiço(s): %s\nTotal: %s",
                            client.getName(), dataFormatada, horaFormatada,
                            serviceList.stream().map(Service::getName).collect(Collectors.joining(",")), ConvertAndFormatUtil.calcularValorTotalFormatado(serviceList)))
                    .company(client.getCompany())
                    .client(client)
                    .start(request.start())
                    .end(request.start().plusHours(1))
                    .provider(provider)
                    .build();

            serviceList.forEach(appointment::addService);
            appointmentService.save(appointment);
            return ResponseEntity.status(HttpStatus.OK).body(appointment);

        } else {
            return ResponseEntity.status(HttpStatus.OK)
                        .body(Map.of(
                                "message", "Dia e horário não disponível para agendamento."
                        ));
        }

    }

    @PutMapping("/change-status")
    public ResponseEntity<?> changeStatus(@RequestBody AppointmentChangeStatusRequest request) {
        Appointment appointment = appointmentService.findById(request.appointmentId());
        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(request.appointmentStatus());
        appointment.setStatus(appointmentStatus);

        appointmentService.save(appointment);

        return  ResponseEntity.status(HttpStatus.OK)
                .body(Map.of(
                        "message", "Alteração de status feito com sucesso."
                ));

    }

    @PutMapping("/change-appointment")
    public ResponseEntity<?> changeAppointment(@RequestBody @Valid AppointmentChangeRequest request) {
        Appointment appointment = appointmentService.findById(request.appointmentId());

//        LocalDate appointmentDate = request.start().toLocalDate();
        List<Service> serviceList = serviceService.findAllById(request.serviceIds());
        boolean isAvailable = appointmentService.isAvailable(appointment.getCompany().getId(), appointment.getProvider().getId(),
                request.start(), request.start().plusHours(1), UUID.fromString(request.specialistId()));

        if (appointment.getStart().equals(request.start())) {
            isAvailable = true;
        }

        if (isAvailable) {

            DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
            String dataFormatada = request.start().format(formatoData);
            String horaFormatada = request.start().format(formatoHora);

            appointment.setStart(request.start());
            appointment.setEnd(request.start().plusHours(1));
            appointment.setNotes(String.format("Nome: %s\nData do Agendamento: %s\nHorário: %s\nServiço(s): %s\nTotal: %s",
                            appointment.getClient().getName(), dataFormatada, horaFormatada,
                            serviceList.stream().map(Service::getName).collect(Collectors.joining(",")), ConvertAndFormatUtil.calcularValorTotalFormatado(serviceList)));
            appointment.updateServices(serviceList);
            appointmentService.save(appointment);
            return ResponseEntity.status(HttpStatus.OK).body(appointment);

        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of(
                            "message", "Dia e horário não disponível para agendamento."
                    ));
        }
    }

    @GetMapping("/checkAvailability")
    public ResponseEntity<?> checkAvailability(@RequestBody @Valid AppointmentRequest request) {
        String providerId = "";
        if (request.providerId() == null) {
            Optional<ProviderMinAppointmentsDto> provider = providerService.findProviderWithLeastAppointments(
                    request.companyId(), request.specialistId(), request.start().toLocalDate());
            providerId = provider.get().getId().toString();
        } else {
            providerId = request.providerId();
        }

        if (appointmentService.isAvailable(UUID.fromString(request.companyId()), UUID.fromString(providerId),
                request.start(), request.start().plusHours(1), UUID.fromString(request.specialistId()))) {
            return ResponseEntity.status(HttpStatus.OK)
                            .body(Map.of(
                                    "message", "Dia e horário disponível para agendamento.",
                                    "available", true
                            ));

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of(
                        "message", "Dia e horário não disponível para agendamento.",
                        "available", false
                ));
    }

    @GetMapping("/find-best-slot")
    public ResponseEntity<?> findBestSlot(@RequestBody @Valid AppointmentRequest request) {
        return appointmentService.findBestSlot(request)
                .map(slot -> ResponseEntity.ok(Map.of(
                        "providerId", slot.providerId(),
                        "startTime", slot.startTime()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/finalize-missed-appointments")
    public ResponseEntity<?> finalizeMissedAppointments() {
        List<Appointment> appointments = appointmentService.pastAppointmentsScheduled();
        if (appointments != null && !appointments.isEmpty()) {
            List<UUID> appointmentIds = appointments.stream()
                    .map(Appointment::getId)
                    .toList();
            appointmentService.cancelAppointmentsBatch(appointmentIds);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of(
                            "message", String.format("Foram atualizados %s agendamentos passados.", appointments.size()),
                            "idsAtualizados", appointments.stream().map(a -> a.getId().toString()).collect(Collectors.joining(", "))
                    ));

        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of(
                            "message", "Não há agendamentos para cancelar",
                            "idsAtualizados", ""
                    ));
        }
    }

    @GetMapping("/next")
    public ResponseEntity<?> getNextAvailableTimes(@RequestBody AppointmentRequest request) {
        return appointmentService.findNextAvailableTimes(UUID.fromString(request.companyId()), request.serviceIds().stream().map(str -> {
                    try {
                        return UUID.fromString(str);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()), request.start().toLocalDate())
                .map(dto -> ResponseEntity.ok(Map.of(
                        "date", dto.date(),
                        "availableTimes", dto.availableTimes().stream()
                                .map(t -> t.format(DateTimeFormatter.ofPattern("HH:mm")))
                                .toList()
                )))
                .orElse(ResponseEntity.notFound().build());
    }


}
