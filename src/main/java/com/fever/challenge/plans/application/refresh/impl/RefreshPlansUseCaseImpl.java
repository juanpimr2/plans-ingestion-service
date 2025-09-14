// src/main/java/com/fever/challenge/plans/application/refresh/impl/RefreshPlansUseCaseImpl.java
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
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshPlansUseCaseImpl implements RefreshPlansUseCase {

    private final PlanRepositoryPort planRepository;
    private final ProviderClientPort providerClient;

    /** Pool pequeño para el trabajo en background. */
    private static final ExecutorService POOL =
            Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));

    @Override
    public void refreshNonBlocking(Duration timeBudget) {
        long budgetMs = (timeBudget == null) ? 0 : timeBudget.toMillis();
        log.debug("Scheduling non-blocking refresh (budget={}ms)...", budgetMs);

        CompletableFuture
                .supplyAsync(providerClient::fetchPlans, POOL)
                .orTimeout(Math.max(1, budgetMs), TimeUnit.MILLISECONDS)
                .whenComplete((plans, ex) -> {
                    if (ex != null) {
                        log.warn("Non-blocking refresh failed or timed out after {}ms", budgetMs, ex);
                        return;
                    }
                    if (plans == null || plans.isEmpty()) {
                        log.info("Non-blocking refresh completed: provider returned no plans.");
                        return;
                    }
                    try {
                        planRepository.upsertAll(plans);
                        log.info("Persisted {} plans (non-blocking).", plans.size());
                    } catch (Exception persistEx) {
                        log.error("Failed to persist plans (non-blocking).", persistEx);
                    }
                });
    }

    @Override
    public void refreshBlocking(Duration timeout) {
        long start = System.currentTimeMillis();
        try {
            log.debug("Starting blocking refresh (timeout={}ms)...",
                    timeout == null ? null : timeout.toMillis());

            List<Plan> plans;

            if (timeout == null || timeout.isZero() || timeout.isNegative()) {
                // Sin límite explícito.
                plans = providerClient.fetchPlans();
            } else {
                // Forzamos límite con un future.
                CompletableFuture<List<Plan>> future =
                        CompletableFuture.supplyAsync(providerClient::fetchPlans, POOL);
                plans = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            }

            if (Objects.requireNonNullElse(plans, List.of()).isEmpty()) {
                log.info("Blocking refresh completed: provider returned no plans.");
            } else {
                planRepository.upsertAll(plans);
                log.info("Blocking refresh completed in {}ms. Persisted {} plans.",
                        (System.currentTimeMillis() - start), plans.size());
            }
        } catch (TimeoutException te) {
            log.warn("Blocking refresh timed out after {}ms.",
                    timeout.toMillis(), te);
        } catch (Exception e) {
            log.error("Blocking refresh failed.", e);
        }
    }

    @Override
    public boolean hasAny() {
        return planRepository.hasAny();
    }
}
