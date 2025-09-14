package com.fever.challenge.plans.application.orchestration.impl;

import com.fever.challenge.plans.application.orchestration.SearchWithWarmupUseCase;
import com.fever.challenge.plans.application.refresh.RefreshPlansUseCase;
import com.fever.challenge.plans.application.search.SearchPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
public class SearchWithWarmupUseCaseImpl implements SearchWithWarmupUseCase {

    private final SearchPlansUseCase search;
    private final RefreshPlansUseCase refresh;

    public SearchWithWarmupUseCaseImpl(SearchPlansUseCase search,
                                       RefreshPlansUseCase refresh) {
        this.search = search;
        this.refresh = refresh;
    }

    @Override
    public List<Plan> execute(Instant startsAt, Instant endsAt, Duration warmupBudget) {
        // Cold start: try a short blocking refresh with a strict time budget.
        if (!refresh.hasAny()) {
            log.info("No cached plans in DB. Running blocking warm-up (budget={})...", warmupBudget);
            refresh.refreshBlocking(warmupBudget);
            return search.findWithin(startsAt, endsAt);
        }

        List<Plan> result = search.findWithin(startsAt, endsAt);
        refresh.refreshNonBlocking(warmupBudget);
        return result;
    }
}
