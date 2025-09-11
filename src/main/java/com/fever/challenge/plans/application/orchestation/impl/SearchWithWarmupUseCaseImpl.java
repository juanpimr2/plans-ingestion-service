package com.fever.challenge.plans.application.orchestation.impl;

import com.fever.challenge.plans.application.orchestation.SearchWithWarmupUseCase;
import com.fever.challenge.plans.application.refresh.RefreshPlansUseCase;
import com.fever.challenge.plans.application.search.SearchPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class SearchWithWarmupUseCaseImpl implements SearchWithWarmupUseCase {

    private final RefreshPlansUseCase refreshUseCase;
    private final SearchPlansUseCase searchUseCase;

    public SearchWithWarmupUseCaseImpl(RefreshPlansUseCase refreshUseCase,
                                       SearchPlansUseCase searchUseCase) {
        this.refreshUseCase = refreshUseCase;
        this.searchUseCase = searchUseCase;
    }

    @Override
    public List<Plan> execute(Instant startsAt, Instant endsAt, Duration warmupBudget) {
        refreshUseCase.refreshNonBlocking(warmupBudget);   // no bloquea
        return searchUseCase.findWithin(startsAt, endsAt); // siempre BBDD
    }
}
