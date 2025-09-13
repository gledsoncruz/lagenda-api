package com.lasystems.lagenda.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface CompanyClosureDto {
    UUID getId();
    LocalDate getDate();
    String getReason();
    LocalTime getStartTime();
    LocalTime getEndTime();
}
