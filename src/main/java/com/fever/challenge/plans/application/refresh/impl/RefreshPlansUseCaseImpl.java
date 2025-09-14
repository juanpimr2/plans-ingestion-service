package com.fever.challenge.plans.application.refresh.impl;

import com.fever.challenge.plans.application.refresh.RefreshPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import com.fever.challenge.plans.domain.port.ProviderClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;


/**
 * Implementation of the {@link RefreshPlansUseCase} for refreshing plans from a provider.
 * <p>
 * Provides both non-blocking and blocking methods to refresh plans, supporting optional time budgets
 * and handling persistence and provider errors with appropriate logging.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshPlansUseCaseImpl implements RefreshPlansUseCase {

    private final PlanRepositoryPort planRepository;
    private final ProviderClientPort providerClient;

    /** Small pool for background work. */
    private static final ExecutorService POOL =
            Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));

/**
 * Performs a non-blocking refresh of plans from the provider, respecting a time budget.
 * <p>
 * The operation is executed asynchronously. If the time budget is reached before completion,
 * the operation is cancelled and a warning is logged. If the provider returns no plans,
 * an informational message is logged. Any persistence errors are also logged.
 * </p>
 *
 * @param timeBudget the maximum duration allowed for the refresh; if {@code null} or zero, no time limit is applied
 */
@Override
public void refreshNonBlocking(Duration timeBudget) {
    long budgetMs = Optional.ofNullable(timeBudget)
            .filter(duration -> !duration.isZero() && !duration.isNegative())
            .map(Duration::toMillis)
            .orElse(0L);

    log.debug("Scheduling non-blocking refresh (budget={}ms)...", budgetMs);

    CompletableFuture
            .supplyAsync(providerClient::fetchPlans, POOL)
            .orTimeout(Math.max(1, budgetMs), TimeUnit.MILLISECONDS)
            .whenComplete((plans, ex) -> {
                if (Objects.nonNull(ex)) {
                    log.warn("Non-blocking refresh failed or timed out after {}ms", budgetMs, ex);
                    return;
                }
                Optional.ofNullable(plans)
                        .filter(list -> !list.isEmpty())
                        .ifPresentOrElse(
                                list -> {
                                    try {
                                        planRepository.upsertAll(list);
                                        log.info("Persisted {} plans (non-blocking).", list.size());
                                    } catch (Exception persistEx) {
                                        log.error("Failed to persist plans (non-blocking).", persistEx);
                                    }
                                },
                                () -> log.info("Non-blocking refresh completed: provider returned no plans.")
                        );
            });
}


/**
 * Performs a blocking refresh of plans from the provider, with an optional timeout.
 * <p>
 * If a timeout is specified and is positive, the fetch operation will be executed asynchronously
 * and will block until the timeout expires or the operation completes. If no timeout is provided,
 * the fetch will be performed synchronously.
 * </p>
 *
 * @param timeout the maximum duration to wait for the provider response; if {@code null}, zero, or negative,
 *                the fetch will not be time-limited
 */
@Override
public void refreshBlocking(Duration timeout) {
    long start = System.currentTimeMillis();
    try {
        log.debug("Starting blocking refresh (timeout={}ms)...",
                Objects.toString(timeout, null) == null ? null : timeout.toMillis());

        List<Plan> plans = Optional.ofNullable(timeout)
                .filter(duration -> !duration.isZero() && !duration.isNegative())
                .map(duration -> {
                    try {
                        return CompletableFuture.supplyAsync(providerClient::fetchPlans, POOL)
                                .get(duration.toMillis(), TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                })
                .orElseGet(providerClient::fetchPlans);

        if (Objects.requireNonNullElse(plans, List.of()).isEmpty()) {
            log.info("Blocking refresh completed: provider returned no plans.");
        } else {
            planRepository.upsertAll(plans);
            log.info("Blocking refresh completed in {}ms. Persisted {} plans.",
                    (System.currentTimeMillis() - start), plans.size());
        }
    } catch (CompletionException ce) {
        if (ce.getCause() instanceof TimeoutException te) {
            log.warn("Blocking refresh timed out after {}ms.",
                    timeout.toMillis(), te);
        } else {
            log.error("Blocking refresh failed.", ce.getCause());
        }
    } catch (Exception e) {
        log.error("Blocking refresh failed.", e);
    }
}


}
