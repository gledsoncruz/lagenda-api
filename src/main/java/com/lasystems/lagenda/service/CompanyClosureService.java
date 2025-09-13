package com.lasystems.lagenda.service;

import com.lasystems.lagenda.models.CompanyClosure;
import com.lasystems.lagenda.repository.CompanyClosureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyClosureService {

    private final CompanyClosureRepository companyClosureRepository;

    /**
     * Verifica se o período (start a end) está em um fechamento da empresa.
     *
     * @param companyId ID da empresa
     * @param start     Início do agendamento
     * @param end       Fim do agendamento
     * @return true se NÃO houver conflito (ou seja, está disponível)
     */
    public boolean isAvailableForScheduling(UUID companyId, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return false;

        LocalDate scheduleDate = start.toLocalDate();

        return companyClosureRepository.findByCompanyIdAndDate(companyId, scheduleDate)
                .stream()
                .noneMatch(closure -> closure.overlapsWith(start, end));
    }

    /**
     * Retorna todos os fechamentos de uma empresa.
     */
    public List<CompanyClosure> getCompanyClosures(UUID companyId) {
        return companyClosureRepository.findByCompanyId(companyId);
    }

}
