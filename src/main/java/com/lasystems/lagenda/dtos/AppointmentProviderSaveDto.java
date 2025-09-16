package com.lasystems.lagenda.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
public class AppointmentProviderSaveDto {
    private UUID id;
    private String name;
    private String calendarId;
}
