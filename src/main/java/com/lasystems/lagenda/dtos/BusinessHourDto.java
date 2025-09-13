package com.lasystems.lagenda.dtos;

import java.time.LocalTime;
import java.util.UUID;

public interface BusinessHourDto {

    UUID getId();
    CompanyDto getCompany();
    Integer getDayOfWeek();
    LocalTime getStartTime();
    LocalTime getEndTime();
    CompanyClosureDto getCompanyClosures();

}
