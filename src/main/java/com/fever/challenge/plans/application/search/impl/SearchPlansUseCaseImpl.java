package com.fever.challenge.plans.application.search.impl;

import com.fever.challenge.plans.application.search.SearchPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Implementation of the {@link SearchPlansUseCase} interface.
 * Provides methods to search for plans within a given time range.
 */
@Component
@RequiredArgsConstructor
class SearchPlansUseCaseImpl implements SearchPlansUseCase {

    private final PlanService service;


    /**
     * Finds all plans that start within the specified time range.
     *
     * @param startsAt the start of the time range (inclusive)
     * @param endsAt the end of the time range (exclusive)
     * @return a list of plans within the specified time range
     */
    @Override
    public List<Plan> findWithin(Instant startsAt, Instant endsAt) {
        return service.findWithin(startsAt, endsAt);
    }
}
