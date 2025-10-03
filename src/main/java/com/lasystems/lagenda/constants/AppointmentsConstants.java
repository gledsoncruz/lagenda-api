package com.lasystems.lagenda.constants;

public final class AppointmentsConstants {

    private AppointmentsConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Durações
    public static final int DEFAULT_SLOT_DURATION_MINUTES = 60;
    public static final int MAX_SEARCH_DAYS_AHEAD = 5;
    public static final int SLOT_INTERVAL_MINUTES = 60;

    // Timezone
    public static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";

    // Horários comerciais padrão
    public static final int DEFAULT_BUSINESS_START_HOUR = 9;
    public static final int DEFAULT_BUSINESS_END_HOUR = 18;

    /**
     * Operações do Google Calendar para integração N8N.
     */
    public static final class GoogleCalendarOperation {
        public static final int CREATE_EVENT = 1;
        public static final int UPDATE_EVENT = 2;
        public static final int CANCEL_EVENT = 3;

        private GoogleCalendarOperation() {
            throw new UnsupportedOperationException("Utility class");
        }
    }

    /**
     * Mensagens de resposta padrão.
     */
    public static final class Messages {
        public static final String APPOINTMENT_CREATED = "Agendamento criado com sucesso.";
        public static final String APPOINTMENT_UPDATED = "Agendamento alterado com sucesso.";
        public static final String STATUS_CHANGED = "Status alterado com sucesso.";
        public static final String SLOT_NOT_AVAILABLE = "Dia e horário não disponível para agendamento.";
        public static final String NO_PROVIDER_AVAILABLE = "Nenhum prestador disponível com essa especialidade.";
        public static final String CLIENT_CONFLICT = "Cliente já possui agendamento nesse mesmo dia e horário.";

        private Messages() {
            throw new UnsupportedOperationException("Utility class");
        }
    }

}
