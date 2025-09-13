package com.lasystems.lagenda.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentDto {

        UUID getId();
        LocalDateTime getStart();
        LocalDateTime getEnd();
        String getStatus();
        String getEventId();
        List<ServiceDto> getServices();
//        ProviderDto getProvider();


}
