package com.fever.challenge.plans.adapters.out.persistence.repo.impl;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.adapters.out.persistence.mapper.PlanPersistenceMapper;
import com.fever.challenge.plans.adapters.out.persistence.repo.PlanRepository;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Domain adapter for plan persistence.
 * Named “Adapter” instead of “Impl” to prevent Spring Data from treating it as a JPA repository implementation,
 * which previously caused a dependency cycle.
 */
@Repository
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PlanRepositoryAdapter implements PlanRepositoryPort {

    public static final String ONLINE = "online";
    public static final String PLAN_ID_CANNOT_BE_NULL = "Plan id cannot be null";

    private final PlanRepository planRepository;
    private final PlanPersistenceMapper mapper;

    @Override
    public void upsertAll(List<Plan> plans) {
        log.info("upsertAll called: processing {} plans…", plans.size());

        Instant now = Instant.now();
        plans.stream()
                .filter(Objects::nonNull)
                .map(plan -> {
                    String providerId = Objects.requireNonNull(plan.getId(), PLAN_ID_CANNOT_BE_NULL);
                    String providerIdStr = String.valueOf(providerId);

                    Optional<PlanEntity> existing = planRepository.findByProviderId(providerIdStr);

                    PlanEntity entity = existing.orElseGet(() -> {
                        log.info("Creating new plan entity (providerId={})", providerIdStr);
                        PlanEntity planEntity = mapper.toEntity(plan);
                        planEntity.setFirstSeenAt(now);
                        return planEntity;
                    });

                    existing.ifPresent(planEntity -> {
                        log.info("Updating existing plan entity (providerId={})", providerIdStr);
                        mapper.updateEntityFromDomain(plan, entity);
                    });

                    entity.setSellMode(ONLINE);
                    entity.setCurrentlyAvailable(true);
                    entity.setLastSeenAt(now);

                    if (!Objects.equals(entity.getProviderId(), providerIdStr)) {
                        log.info("ProviderId changed -> updating entity providerId from {} to {}", entity.getProviderId(), providerIdStr);
                        entity.setProviderId(providerIdStr);
                    }

                    return entity;
                })
                .forEach(planRepository::save);

        log.info("upsertAll completed: {} plans processed.", plans.size());
    }


    /**
     * Finds {@link Plan} in ONLINE sell mode that overlap the given time window.
     *
     * <p><strong>Overlap rule</strong>: {@code plan.startsAt < end && plan.endsAt > start}</p>
     *
     * @param start window start (UTC)
     * @param end   window end (UTC)
     * @return plans overlapping the window (possibly empty, never {@code null})
     */
    @Override
    @Transactional(readOnly = true)
    public List<Plan> findOverlapOnline(Instant start, Instant end) {
        log.info("findOverlapOnline called: start={}, end={}", start, end);

        List<Plan> result = planRepository
                .findBySellModeAndStartsAtBeforeAndEndsAtAfter(ONLINE, end, start)
                .stream()
                .map(mapper::toDomain)
                .toList();

        log.info("findOverlapOnline returning {} plans.", result.size());
        return result;
    }


    /**
     * Checks whether there is at least one persisted {@link Plan}.
     *
     * @return {@code true} if any plan exists; {@code false} otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasAny() {
        long count = planRepository.count();
        boolean any = count > 0;
        log.info("hasAny: {} (count={})", any, count);
        return any;
    }


}
