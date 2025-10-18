package com.lasystems.lagenda.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Configuração de timezone para a aplicação.
 *
 * ESTRATÉGIA:
 * - Banco de dados: SEMPRE em UTC (timestamptz)
 * - Java/JPA: SEMPRE em UTC internamente
 * - JSON API: SEMPRE com timezone America/Sao_Paulo
 * - Frontend: Recebe datas já no timezone correto
 */
@Configuration
public class TimezoneConfig {

    private static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";

    /**
     * Injeta o ObjectMapper que já existe (criado pelo Spring Boot)
     * ao invés de criar um novo.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Configura timezone padrão da JVM e do ObjectMapper existente.
     */
    @PostConstruct
    public void init() {
        // 1. Configurar timezone padrão da JVM
        TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIMEZONE));

        // 2. Configurar o ObjectMapper existente
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}