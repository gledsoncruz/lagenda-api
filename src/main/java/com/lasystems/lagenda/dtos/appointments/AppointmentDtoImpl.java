package com.lasystems.lagenda.dtos.appointments;

import com.lasystems.lagenda.dtos.AppointmentDto;
import com.lasystems.lagenda.dtos.ServiceDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AppointmentDtoImpl implements AppointmentDto {
    private UUID id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;
    private String eventId;
    private List<ServiceDto> services;

    public AppointmentDtoImpl(UUID id, LocalDateTime start, LocalDateTime end, String status, String eventId, List<ServiceDto> services) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.status = status;
        this.eventId = eventId;
        this.services = services;
    }

    // getters
    @Override
    public UUID getId() { return id; }
    @Override
    public LocalDateTime getStart() { return start; }
    @Override
    public LocalDateTime getEnd() { return end; }
    @Override
    public String getStatus() { return status; }
    @Override
    public String getEventId() { return eventId; }
    @Override
    public List<ServiceDto> getServices() { return services; }
}
