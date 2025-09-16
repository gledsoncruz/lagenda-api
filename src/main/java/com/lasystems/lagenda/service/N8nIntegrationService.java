package com.lasystems.lagenda.service;

import com.lasystems.lagenda.models.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class N8nIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${n8n.google.calendar.webhook.url}")
    private String n8nWebhookUrl;

    public void notifyGoogleCalendarN8N(Appointment appointment, int op) {
//        String n8nWebhook = "https://automations-n8n.7suddb.easypanel.host/webhook/agendar";

        Map<String, Object> payload = Map.of(
                "appointmentId", appointment.getId(),
                "title", "Agendamento com " + appointment.getProvider().getName(),
                "start", appointment.getStart().toString(),
                "end", appointment.getEnd().toString(),
                "notes", appointment.getNotes(),
                "calendarId", appointment.getProvider().getCalendarId(),
                "op", op,
                "eventId", op != 1 ? appointment.getEventId() : ""
        );

        try {
            restTemplate.postForEntity(n8nWebhookUrl, payload, String.class);
        } catch (Exception e) {

            // Logue erro, mas não falhe o agendamento principal
//            log.error("Falha ao notificar n8n", e);
        }
    }

}
