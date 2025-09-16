package com.lasystems.lagenda.dtos;

import com.lasystems.lagenda.models.Provider;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Builder
@Getter
@Setter
public class AppointmentSaveDto {

    private UUID id;
    private String status;
    private String eventId;
    private String calendarId;
    private String notes;
    private AppointmentProviderSaveDto provider;
    private LocalDateTime start;
    private LocalDateTime end;

}
