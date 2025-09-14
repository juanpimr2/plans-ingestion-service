package com.fever.challenge.plans.application.orchestration.impl;

import com.fever.challenge.plans.adapters.out.persistence.repo.PlanRepository;
import com.fever.challenge.plans.application.orchestration.SearchWithWarmupUseCase;
import com.fever.challenge.plans.application.refresh.RefreshPlansUseCase;
import com.fever.challenge.plans.application.search.SearchPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Use case implementation for searching plans with a warm-up strategy.
 *
 * <p>
 * This class provides a search operation that first checks for cached data.
 * If cached data is available, it returns the results immediately and triggers a background refresh.
 * If no cached data is present (cold start), it performs a blocking warm-up within the specified budget before returning results.
 * </p>
 *
 * <p>
 * Implements the {@link SearchWithWarmupUseCase} interface.
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SearchWithWarmupUseCaseImpl implements SearchWithWarmupUseCase {

    private final SearchPlansUseCase search;
    private final RefreshPlansUseCase refresh;
    private final PlanRepository planRepository;

    /**
     * Executes a bounded search for {@link Plan} between the given instants.
     *
     * <p><strong>Implementation notes</strong>:</p>
     * <ul>
     *   <li><em>Fast path</em>: If cached data is available, return results immediately and kick a background refresh (stale-while-revalidate).</li>
     *   <li><em>Cold start</em>: If there’s no data yet, run a short blocking warm-up (within the given budget) before answering.</li>
     * </ul>
     *
     * @param startsAt     inclusive lower bound of the search window (UTC)
     * @param endsAt       exclusive upper bound of the search window (UTC)
     * @param warmupBudget max duration allowed for warm-up/refresh operations
     * @return plans within the time window (possibly empty, never {@code null})
     */
    public List<Plan> execute(Instant startsAt, Instant endsAt, Duration warmupBudget) {
        // Fast path: cached data ⇒ return now, refresh in background.
        // Cold start: no data ⇒ warm up briefly, then answer.

        if (!planRepository.hasAny()) {
            log.info("No cached plans. Running a short blocking warm-up (budget={}).", warmupBudget);
            refresh.refreshBlocking(warmupBudget);
            return search.findWithin(startsAt, endsAt);
        }

        List<Plan> result = search.findWithin(startsAt, endsAt);
        log.info("Returning {} plans. Background refresh started.", result.size());
        refresh.refreshNonBlocking(warmupBudget);
        return result;
    }


}
