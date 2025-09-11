package com.fever.challenge.plans.domain.service;

import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;

import java.time.Instant;
import java.util.List;

public class PlanService {

    private final PlanRepositoryPort repository;

    public PlanService(PlanRepositoryPort repository) {
        this.repository = repository;
    }

    /** Consulta planes con solapamiento en la ventana [start, end] y sell_mode=online */
    public List<Plan> findWithin(Instant start, Instant end) {
        return repository.findOverlapOnline(start, end);
    }
}
