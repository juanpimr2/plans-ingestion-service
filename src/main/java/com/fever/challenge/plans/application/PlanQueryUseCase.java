package com.fever.challenge.plans.application;

import com.fever.challenge.plans.adapters.in.rest.dto.PlanDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class PlanQueryUseCase {

    public List<PlanDto> findWithin(Instant startsAt, Instant endsAt) {
        // Stub inicial: devuelve lista vacía
        return List.of();
    }
}
