package com.lasystems.lagenda.service;

import com.lasystems.lagenda.models.BusinessHour;
import com.lasystems.lagenda.repository.BusinessHourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Service para gerenciamento de horários comerciais.
 * Implementa cache para melhorar performance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessHourService {

    private final BusinessHourRepository businessHourRepository;

    /**
     * Verifica se o período (start a end) está dentro do horário comercial da empresa.
     * Método com cache para melhor performance.
     *
     * @param companyId ID da empresa
     * @param start     Início do agendamento
     * @param end       Fim do agendamento
     * @return true se estiver dentro do horário comercial
     */
    public boolean isWithinBusinessHours(UUID companyId, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            log.warn("Data de início ou fim nula para company={}", companyId);
            return false;
        }

        int dayOfWeek = toDatabaseDayOfWeek(start.getDayOfWeek());
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        List<BusinessHour> businessHours = findByCompanyIdAndDayOfWeek(companyId, dayOfWeek);

        boolean isWithin = businessHours.stream()
                .anyMatch(bh -> {
                    LocalTime bhStart = bh.getStartTime();
                    LocalTime bhEnd = bh.getEndTime();
                    return !startTime.isBefore(bhStart) && !endTime.isAfter(bhEnd);
                });

        log.debug("Verificação de horário comercial: company={}, day={}, isWithin={}",
                companyId, dayOfWeek, isWithin);

        return isWithin;
    }

    /**
     * Retorna os horários comerciais de uma empresa em um dia da semana.
     * Resultado é cacheado para melhor performance.
     *
     * @param companyId ID da empresa
     * @param dayOfWeek dia da semana (0=Domingo, 1=Segunda, ..., 6=Sábado)
     * @return lista de horários comerciais
     */
    @Cacheable(value = "businessHours", key = "#companyId + '-' + #dayOfWeek")
    public List<BusinessHour> findByCompanyIdAndDayOfWeek(UUID companyId, Integer dayOfWeek) {
        log.debug("Buscando horários comerciais: company={}, day={}", companyId, dayOfWeek);
        return businessHourRepository.findByCompanyIdAndDayOfWeek(companyId, dayOfWeek);
    }

    /**
     * Retorna todos os horários comerciais de uma empresa.
     *
     * @param companyId ID da empresa
     * @return lista de horários comerciais
     */
    @Cacheable(value = "businessHours", key = "#companyId")
    public List<BusinessHour> getBusinessHours(UUID companyId) {
        log.debug("Buscando todos os horários comerciais: company={}", companyId);
        return businessHourRepository.findByCompanyId(companyId);
    }

    /**
     * Converte DayOfWeek do Java para o formato do banco de dados.
     * Java: 1=Segunda, 7=Domingo
     * Banco: 0=Domingo, 1=Segunda, ..., 6=Sábado
     *
     * @param javaDayOfWeek dia da semana do Java
     * @return dia da semana no formato do banco
     */
    public Integer toDatabaseDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
        return javaDayOfWeek.getValue() % 7;
    }
}