package com.fever.challenge.plans.application;

import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.service.PlanService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class PlanQueryUseCase {
    private final PlanService service;

    public PlanQueryUseCase(PlanService service) {
        this.service = service;
    }

    public List<Plan> findWithin(Instant startsAt, Instant endsAt) {
        return service.findWithin(startsAt, endsAt);
    }
}
