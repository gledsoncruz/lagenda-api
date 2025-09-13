package com.lasystems.lagenda.dtos;

import com.lasystems.lagenda.models.Appointment;

import java.util.List;

public interface ProviderDto {

        String getName();
        String getCalendarId();
        String getPhone();
        List<AppointmentDto> getAppoinments();

}
