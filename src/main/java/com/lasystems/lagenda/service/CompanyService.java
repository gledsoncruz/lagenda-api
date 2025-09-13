package com.lasystems.lagenda.service;

import com.lasystems.lagenda.dtos.CompanyDto;
import com.lasystems.lagenda.exceptions.EntityNotFoundException;
import com.lasystems.lagenda.exceptions.UUIDIllegalArgumentException;
import com.lasystems.lagenda.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository repo;

    public Optional<CompanyDto> getById(String id) {
        try {
            Optional<CompanyDto> companyDtoOptional = repo.findCompanyDto(UUID.fromString(id));
            if (companyDtoOptional.isPresent()) {
                return companyDtoOptional;
            } else {
                throw new EntityNotFoundException();
            }
        } catch(IllegalArgumentException ex) {
            throw new UUIDIllegalArgumentException();
        }
    }
}
