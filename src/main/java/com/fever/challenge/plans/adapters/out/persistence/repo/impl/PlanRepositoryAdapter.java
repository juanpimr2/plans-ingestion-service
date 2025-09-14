package com.fever.challenge.plans.adapters.out.persistence.repo.impl;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.adapters.out.persistence.mapper.PlanPersistenceMapper;
import com.fever.challenge.plans.adapters.out.persistence.repo.PlanRepository;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de dominio para persistencia de planes.
 * Se llama “Adapter” en lugar de “Impl” para evitar que Spring Data lo trate como implementación del JPA repository,
 * lo que producía el ciclo de dependencias.
 */
@Repository
@RequiredArgsConstructor
@Transactional
public class PlanRepositoryAdapter implements PlanRepositoryPort {

    public static final String ONLINE = "online";
    public static final String PLAN_ID_CANNOT_BE_NULL = "Plan id cannot be null";

    private final PlanRepository planRepository;
    private final PlanPersistenceMapper mapper;

    @Override
    public void upsertAll(List<Plan> plans) {
        Instant now = Instant.now();
        plans.stream()
                .filter(Objects::nonNull)
                .map(plan -> {
                    String providerId = Objects.requireNonNull(plan.getId(), PLAN_ID_CANNOT_BE_NULL);
                    String providerIdStr = String.valueOf(providerId);

                    Optional<PlanEntity> existing = planRepository.findByProviderId(providerIdStr);

                    PlanEntity entity = existing.orElseGet(() -> {
                        PlanEntity planEntity = mapper.toEntity(plan);
                        planEntity.setFirstSeenAt(now);
                        return planEntity;
                    });

                    existing.ifPresent(planEntity -> mapper.updateEntityFromDomain(plan, entity));

                    entity.setSellMode(ONLINE);
                    entity.setCurrentlyAvailable(true);
                    entity.setLastSeenAt(now);

                    if (!Objects.equals(entity.getProviderId(), providerIdStr)) {
                        entity.setProviderId(providerIdStr);
                    }

                    return entity;
                })
                .forEach(planRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> findOverlapOnline(Instant start, Instant end) {
        return planRepository.findBySellModeAndStartsAtBeforeAndEndsAtAfter(ONLINE, end, start)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAny() {
        return planRepository.count() > 0;
    }

}
