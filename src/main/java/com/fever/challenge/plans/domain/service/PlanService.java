package com.fever.challenge.plans.domain.service;

import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import java.time.Instant;
import java.util.List;

public class PlanService {
    private final PlanRepositoryPort repo;

    public PlanService(PlanRepositoryPort repo) { this.repo = repo; }

    public List<Plan> queryPlans(Instant start, Instant end) {
        return repo.findOverlapOnline(start, end);
    }
}
