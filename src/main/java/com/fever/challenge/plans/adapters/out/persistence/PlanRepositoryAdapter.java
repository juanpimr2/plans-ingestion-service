package com.fever.challenge.plans.adapters.out.persistence;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.adapters.out.persistence.repo.JpaPlanRepository;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional
public class PlanRepositoryAdapter implements PlanRepositoryPort {

    private final JpaPlanRepository jpa;

    @Override
    public void upsertAll(List<Plan> plans) {
        final Instant now = Instant.now();

        final List<PlanEntity> entities = plans.stream()
                .map(p -> {
                    final String providerId = String.valueOf(p.getId());

                    final PlanEntity e = jpa.findByProviderId(providerId).orElseGet(() -> {
                        PlanEntity ne = new PlanEntity();
                        ne.setProviderId(providerId);
                        ne.setFirstSeenAt(now);
                        return ne;
                    });

                    e.setTitle(p.getTitle());
                    e.setSellMode("online");
                    e.setStartsAt(toInstantUtc(p.getStartDate(), p.getStartTime()));
                    e.setEndsAt(toInstantUtc(p.getEndDate(), p.getEndTime()));
                    e.setMinPrice(p.getMinPrice());
                    e.setMaxPrice(p.getMaxPrice());
                    e.setLastSeenAt(now);
                    e.setCurrentlyAvailable(true);
                    return e;
                })
                .toList();

        jpa.saveAll(entities);
    }

    private static Instant toInstantUtc(LocalDate d, LocalTime t) {
        return (Objects.isNull(d) || Objects.isNull(t))
                ? null
                : ZonedDateTime.of(d, t, ZoneOffset.UTC).toInstant();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> findOverlapOnline(Instant start, Instant end) {
        // solapamiento: starts_at < end  &&  ends_at > start
        return jpa.findBySellModeAndStartsAtBeforeAndEndsAtAfter("online", end, start)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAny() {
        return jpa.count() > 0;
    }

    private Plan toDomain(PlanEntity e) {
        final Optional<LocalDateTime> s = Optional.ofNullable(e.getStartsAt())
                .map(i -> LocalDateTime.ofInstant(i, ZoneOffset.UTC));
        final Optional<LocalDateTime> en = Optional.ofNullable(e.getEndsAt())
                .map(i -> LocalDateTime.ofInstant(i, ZoneOffset.UTC));

        return Plan.builder()
                .id(e.getProviderId()) // ya es String
                .title(e.getTitle())
                .startDate(s.map(LocalDateTime::toLocalDate).orElse(null))
                .startTime(s.map(LocalDateTime::toLocalTime).orElse(null))
                .endDate(en.map(LocalDateTime::toLocalDate).orElse(null))
                .endTime(en.map(LocalDateTime::toLocalTime).orElse(null))
                .minPrice(e.getMinPrice())
                .maxPrice(e.getMaxPrice())
                .build();
    }
}
