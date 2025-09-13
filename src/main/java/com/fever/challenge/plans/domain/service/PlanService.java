package com.fever.challenge.plans.domain.service;

import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class PlanService {

    public static final String START_END_REQUIRED = "start/end required";
    public static final String START_MUST_BE_END = "start must be <= end";
    private final PlanRepositoryPort repository;

    public PlanService(PlanRepositoryPort repository) {
        this.repository = repository;
    }

    /** Consulta planes con solapamiento en la ventana [start, end] y sell_mode=online */
    public List<Plan> findWithin(Instant start, Instant end) {
        if (Objects.isNull(start)|| Objects.isNull(end)) throw new IllegalArgumentException(START_END_REQUIRED);
        if (start.isAfter(end)) throw new IllegalArgumentException(START_MUST_BE_END);
        return repository.findOverlapOnline(start, end);
    }
}
