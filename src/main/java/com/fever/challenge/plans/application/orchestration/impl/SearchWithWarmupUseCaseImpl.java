package com.fever.challenge.plans.application.orchestration.impl;

import com.fever.challenge.plans.application.orchestration.SearchWithWarmupUseCase;
import com.fever.challenge.plans.application.refresh.RefreshPlansUseCase;
import com.fever.challenge.plans.application.search.SearchPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import com.fever.challenge.plans.domain.port.ProviderClientPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class SearchWithWarmupUseCaseImpl implements SearchWithWarmupUseCase {

    private final RefreshPlansUseCase refreshUseCase;
    private final SearchPlansUseCase searchUseCase;
    private final PlanRepositoryPort repository;
    private final ProviderClientPort providerClient;

    public SearchWithWarmupUseCaseImpl(RefreshPlansUseCase refreshUseCase,
                                       SearchPlansUseCase searchUseCase,
                                       PlanRepositoryPort repository,
                                       ProviderClientPort providerClient) {
        this.refreshUseCase = refreshUseCase;
        this.searchUseCase = searchUseCase;
        this.repository = repository;
        this.providerClient = providerClient;
    }

    @Override
    public List<Plan> execute(Instant startsAt, Instant endsAt, Duration warmupBudget) {
        log.info("Search request received for window [{} - {}]", startsAt, endsAt);

        refreshUseCase.refreshNonBlocking(warmupBudget);
        log.debug("Triggered asynchronous refresh with budget={}ms", warmupBudget.toMillis());

        List<Plan> fromDb = searchUseCase.findWithin(startsAt, endsAt);
        if (!fromDb.isEmpty()) {
            log.info("Returning {} plans from database", fromDb.size());
            return fromDb;
        }

        if (!repository.hasAny()) {
            log.warn("Database is empty. Attempting blocking refresh with budget={}ms", warmupBudget.toMillis());
            refreshUseCase.refreshBlockingUpTo(warmupBudget);

            fromDb = searchUseCase.findWithin(startsAt, endsAt);
            if (!fromDb.isEmpty()) {
                log.info("Database warmed up. Returning {} plans", fromDb.size());
                return fromDb;
            }
            log.error("Blocking refresh did not produce results within budget");
        }

        log.warn("No results in database. Falling back to provider.");
        List<Plan> fresh = providerClient.fetchPlans();
        if (fresh.isEmpty()) {
            log.error("Provider returned no plans. Responding with empty result set.");
            return List.of();
        }

        log.info("Provider returned {} plans. Returning and scheduling persistence in database", fresh.size());
        CompletableFuture.runAsync(() -> {
            try {
                repository.upsertAll(fresh);
                log.info("Persisted {} plans in database from provider fallback", fresh.size());
            } catch (Exception e) {
                log.error("Failed to persist plans from provider fallback", e);
            }
        });

        List<Plan> filtered = fresh.stream()
                .filter(p -> overlap(p, startsAt, endsAt))
                .toList();
        log.info("Returning {} filtered plans from provider fallback", filtered.size());
        return filtered;
    }

    private static boolean overlap(Plan p, Instant start, Instant end) {
        Instant s = toInstantUtc(p.getStartDate(), p.getStartTime());
        Instant e = toInstantUtc(p.getEndDate(), p.getEndTime());
        if (Objects.isNull(s) || Objects.isNull(e)) return false;
        return s.isBefore(end) && e.isAfter(start);
    }

    private static Instant toInstantUtc(LocalDate d, LocalTime t) {
        return (Objects.isNull(d) || Objects.isNull(t)) ? null
                : ZonedDateTime.of(d, t, ZoneOffset.UTC).toInstant();
    }
}
