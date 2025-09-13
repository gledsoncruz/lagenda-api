package com.lasystems.lagenda.dtos;

import com.lasystems.lagenda.models.Specialist;

import java.util.List;
import java.util.UUID;

public interface CompanyDto {

        UUID getId();
        String getName();
        String getCategory();
        List<SpecialistDto> getSpecialties();
        List<CompanyProviders> getProviders();

}
