package com.fever.challenge.plans.application.refresh.impl;

import com.fever.challenge.plans.application.refresh.RefreshPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import com.fever.challenge.plans.domain.port.ProviderClientPort;
import lombok.extern.slf4j.Slf4j;
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

    public RefreshPlansUseCaseImpl(ProviderClientPort providerClient, PlanRepositoryPort planRepository) {
        this.providerClient = providerClient;
        this.planRepository = planRepository;
    }

    @Override
    public void refreshNonBlocking(Duration timeBudget) {
        CompletableFuture.runAsync(() -> refreshBlockingUpTo(timeBudget))
                .orTimeout(timeBudget.toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(ex -> null);
    }


    @Override
    public void refreshBlockingUpTo(Duration timeBudget) {
        List<Plan> fresh = providerClient.fetchPlans();
        planRepository.upsertAll(fresh);
    }
}