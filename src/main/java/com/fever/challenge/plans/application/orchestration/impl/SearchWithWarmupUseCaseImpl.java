package com.fever.challenge.plans.application.orchestration.impl;

import com.fever.challenge.plans.application.orchestration.SearchWithWarmupUseCase;
import com.fever.challenge.plans.application.refresh.RefreshPlansUseCase;
import com.fever.challenge.plans.application.search.SearchPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
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
        // 1) Buscar en DB
        var db = search.findWithin(startsAt, endsAt);
        // 2) Disparar warmup en background (no necesita resultados de search)
        refresh.refreshNonBlocking(warmupBudget);
        // 3) Devolver lo que haya en DB (rápido, sin bloquear demasiado)
        return db;
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
