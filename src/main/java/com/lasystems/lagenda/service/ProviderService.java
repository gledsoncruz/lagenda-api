package com.lasystems.lagenda.service;

import com.lasystems.lagenda.dtos.ProviderDto;
import com.lasystems.lagenda.dtos.ProviderMinAppointmentsDto;
import com.lasystems.lagenda.exceptions.EntityNotFoundException;
import com.lasystems.lagenda.exceptions.UUIDIllegalArgumentException;
import com.lasystems.lagenda.models.Client;
import com.lasystems.lagenda.models.Provider;
import com.lasystems.lagenda.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository repo;

    public List<ProviderDto> findProvidersByCompany(UUID id) {
        return repo.findProvidersByCompany(id);
    }

    public Provider findById(String id) {

        try {
            Optional<Provider> provider = repo.findById(UUID.fromString(id));
            if (provider.isEmpty()) {
                throw new EntityNotFoundException();
            }
            return provider.get();
        } catch(IllegalArgumentException ex) {
            throw new UUIDIllegalArgumentException();
        }

    }

    public Optional<ProviderMinAppointmentsDto> findProviderWithLeastAppointments(String companyId, String specialtyId, LocalDate date) {

        try {
            Optional<ProviderMinAppointmentsDto> providerMinAppointmentsDto =
                    repo.findProviderWithLeastAppointments(UUID.fromString(companyId), UUID.fromString(specialtyId), date);
            if (providerMinAppointmentsDto.isEmpty()) {
                throw new EntityNotFoundException();
            }
            return providerMinAppointmentsDto;
        } catch(IllegalArgumentException ex) {
            throw new UUIDIllegalArgumentException();
        }

    }

}
