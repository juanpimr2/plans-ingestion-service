package com.fever.challenge.plans.application.refresh.impl;

import com.fever.challenge.plans.application.refresh.RefreshPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import com.fever.challenge.plans.domain.port.ProviderClientPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RefreshPlansUseCaseImpl implements RefreshPlansUseCase {

    private final ProviderClientPort providerClient;
    private final PlanRepositoryPort planRepository;

    public RefreshPlansUseCaseImpl(ProviderClientPort providerClient,
                                   PlanRepositoryPort planRepository) {
        this.providerClient = providerClient;
        this.planRepository = planRepository;
    }

    @Override
    public void refreshNonBlocking(Duration timeBudget) {
        CompletableFuture
                .supplyAsync(providerClient::fetchPlans)
                .orTimeout(timeBudget.toMillis(), TimeUnit.MILLISECONDS)
                .thenAccept(plans -> {
                    try {
                        planRepository.upsertAll(plans);
                        log.info("Persisted {} plans (non-blocking)", plans.size());
                    } catch (Exception e) {
                        log.error("Persist failed (non-blocking)", e);
                    }
                })
                .exceptionally(ex -> { log.warn("Refresh timed out/failed", ex); return null; });
    }

    @Override
    public void refreshBlockingUpTo(Duration timeBudget) {
        try {
            var future = CompletableFuture.supplyAsync(providerClient::fetchPlans);
            var fresh = future.get(timeBudget.toMillis(), TimeUnit.MILLISECONDS);
            planRepository.upsertAll(fresh);
        } catch (Exception e) {
            log.warn("Blocking refresh failed/timeout", e);
        }
    }

    @Override
    public void persistAsync(List<Plan> plans) {
        CompletableFuture.runAsync(() -> {
            try {
                planRepository.upsertAll(plans);
                log.info("Persisted {} plans (fallback)", plans.size());
            } catch (Exception e) {
                log.error("Persist fallback failed", e);
            }
        });
    }
}