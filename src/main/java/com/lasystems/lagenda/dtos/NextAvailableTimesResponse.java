package com.lasystems.lagenda.dtos;

import java.util.List;

public record NextAvailableTimesResponse(
        String date,
        List<String> availableTimes
) {
}
