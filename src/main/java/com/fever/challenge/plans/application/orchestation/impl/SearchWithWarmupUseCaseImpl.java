package com.fever.challenge.plans.application.orchestation.impl;

import com.fever.challenge.plans.application.orchestation.SearchWithWarmupUseCase;
import com.fever.challenge.plans.application.refresh.RefreshPlansUseCase;
import com.fever.challenge.plans.application.search.SearchPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import com.fever.challenge.plans.domain.port.ProviderClientPort;

import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
public class SearchWithWarmupUseCaseImpl implements SearchWithWarmupUseCase {

    private final RefreshPlansUseCase refreshUseCase;
    private final SearchPlansUseCase  searchUseCase;

    // Para decisiones de “frío/caliente” y fallback directo
    private final PlanRepositoryPort  repository;
    private final ProviderClientPort  providerClient;

    public SearchWithWarmupUseCaseImpl(RefreshPlansUseCase refreshUseCase,
                                       SearchPlansUseCase searchUseCase,
                                       PlanRepositoryPort repository,
                                       ProviderClientPort providerClient) {
        this.refreshUseCase = refreshUseCase;
        this.searchUseCase  = searchUseCase;
        this.repository     = repository;
        this.providerClient = providerClient;
    }

    @Override
    public List<Plan> execute(Instant startsAt, Instant endsAt, Duration warmupBudget) {
        // 1) Calentamos sin bloquear
        refreshUseCase.refreshNonBlocking(warmupBudget);

        // 2) Leemos de BBDD
        List<Plan> fromDb = searchUseCase.findWithin(startsAt, endsAt);
        if (!fromDb.isEmpty()) return fromDb;

        // 3) Si estamos “en frío” (BBDD aún vacía), intentamos un calentón bloqueante breve
        if (!repository.hasAny()) {
            refreshUseCase.refreshBlockingUpTo(warmupBudget);
            fromDb = searchUseCase.findWithin(startsAt, endsAt);
            if (!fromDb.isEmpty()) return fromDb;
        }

        // 4) Fallback definitivo: ir directo al provider, devolver en memoria y persistir en background
        List<Plan> fresh = providerClient.fetchPlans();
        if (fresh.isEmpty()) return List.of();

        // Persistimos en segundo plano para no bloquear la respuesta
        CompletableFuture.runAsync(() -> repository.upsertAll(fresh));

        // Filtramos por ventana antes de devolver
        return fresh.stream()
                .filter(p -> overlap(p, startsAt, endsAt))
                .toList();
    }

    private static boolean overlap(Plan p, Instant start, Instant end) {
        // Convierte el (date,time) de dominio a Instant UTC
        Instant s = toInstantUtc(p.getStartDate(), p.getStartTime());
        Instant e = toInstantUtc(p.getEndDate(),   p.getEndTime());
        if (Objects.isNull(s) || Objects.isNull(e)) return false;
        // solapamiento: s < end && e > start
        return s.isBefore(end) && e.isAfter(start);
    }

    private static Instant toInstantUtc(LocalDate d, LocalTime t) {
        return (Objects.isNull(d) || Objects.isNull(t)) ? null
                : ZonedDateTime.of(d, t, ZoneOffset.UTC).toInstant();
    }
}
