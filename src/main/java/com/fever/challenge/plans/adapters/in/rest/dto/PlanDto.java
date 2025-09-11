package com.fever.challenge.plans.adapters.in.rest.dto;

import java.time.Instant;
import java.util.List;

public record PlanDto(
        String providerId,
        String title,
        String sellMode,
        Instant startsAt,
        Instant endsAt,
        List<String> zones,
        boolean currentlyAvailable
) {}
