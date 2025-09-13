package com.lasystems.lagenda.dtos;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record ClientDto(
        UUID id,
        String name,
        String email,
        String phone,
        List<AppointmentDto> appointments,
        JsonNode conversationHistory
) {


}
