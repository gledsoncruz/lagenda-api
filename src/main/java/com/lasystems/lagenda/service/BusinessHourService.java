package com.lasystems.lagenda.service;

import com.lasystems.lagenda.dtos.BusinessHourDto;
import com.lasystems.lagenda.models.BusinessHour;
import com.lasystems.lagenda.repository.BusinessHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessHourService {

    private final BusinessHourRepository businessHourRepository;

//    public List<BusinessHourDto> findBusinessHoursByCompany(UUID id) {
//        return repo.findBusinessHoursByCompany(id);
//    }

    /**
     * Verifica se o período (start a end) está dentro do horário comercial da empresa.
     *
     * @param companyId ID da empresa
     * @param start     Início do agendamento
     * @param end       Fim do agendamento
     * @return true se estiver dentro do horário comercial
     */
    public boolean isWithinBusinessHours(UUID companyId, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return false;

        int dayOfWeek = start.getDayOfWeek().getValue() % 7; // 0 = Domingo, 1 = Segunda, ..., 6 = Sábado
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        return businessHourRepository.findByCompanyIdAndDayOfWeek(companyId, dayOfWeek)
                .stream()
                .anyMatch(bh -> {
                    LocalTime bhStart = bh.getStartTime();
                    LocalTime bhEnd = bh.getEndTime();
                    return !startTime.isBefore(bhStart) && !endTime.isAfter(bhEnd);
                });
    }

    /**
     * Retorna todos os horários comerciais de uma empresa.
     */
    public List<BusinessHour> getBusinessHours(UUID companyId) {
        return businessHourRepository.findByCompanyId(companyId);
    }

    /**
     * Retorna os horários comerciais de uma empresa em um dia da semana.
     * O dayOfWeek segue o padrão: 0 = Domingo, 1 = Segunda, ..., 6 = Sábado
     */
    public List<BusinessHour> findByCompanyIdAndDayOfWeek(UUID companyId, Integer dayOfWeek) {
        return businessHourRepository.findByCompanyIdAndDayOfWeek(companyId, dayOfWeek);
    }

    /**
     * Helper: converte DayOfWeek do Java (1=Segunda, 7=Domingo) para nosso padrão (0=Domingo)
     */
    public Integer toDatabaseDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
        return (javaDayOfWeek.getValue() % 7); // 1->1 (Seg), 7->0 (Dom)
    }
}
