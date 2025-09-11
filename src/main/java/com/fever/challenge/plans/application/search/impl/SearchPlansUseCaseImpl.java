package com.fever.challenge.plans.application.search.impl;

import com.fever.challenge.plans.application.search.SearchPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.service.PlanService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
class SearchPlansUseCaseImpl implements SearchPlansUseCase {

    private final PlanService service;

    SearchPlansUseCaseImpl(PlanService service) {
        this.service = service;
    }

    @Override
    public List<Plan> findWithin(Instant startsAt, Instant endsAt) {
        return service.findWithin(startsAt, endsAt);
    }
}
