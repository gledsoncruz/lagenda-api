package com.lasystems.lagenda.dtos.request;

public record AppointmentChangeStatusRequest(
        String appointmentId,
        String appointmentStatus
) {
}
