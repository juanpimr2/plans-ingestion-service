package com.fever.challenge.plans.adapters.out.persistence;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.adapters.out.persistence.mapper.PlanPersistenceMapper;
import com.fever.challenge.plans.adapters.out.persistence.repo.JpaPlanRepository;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Component
public class PlanRepositoryAdapter implements PlanRepositoryPort {

    private final JpaPlanRepository jpa;
    private final PlanPersistenceMapper mapper;

    public PlanRepositoryAdapter(JpaPlanRepository jpa, PlanPersistenceMapper mapper) {
        this.jpa = jpa; this.mapper = mapper;
    }

    @Override @Transactional
    public void upsertAll(List<Plan> plans) {
        for (Plan p : plans) {
            jpa.findByProviderId(p.getId()).ifPresentOrElse(existing -> {
                PlanEntity updated = mapper.toEntity(p);
                updated.setId(existing.getId());
                // Campos controlados por ingesta
                updated.setSellMode(existing.getSellMode());
                updated.setFirstSeenAt(existing.getFirstSeenAt());
                updated.setCurrentlyAvailable(true);
                updated.setLastSeenAt(Instant.now());
                // min/max del dominio ya vienen calculados
                updated.setMinPrice(p.getMinPrice());
                updated.setMaxPrice(p.getMaxPrice());
                jpa.save(updated);
            }, () -> {
                PlanEntity e = mapper.toEntity(p);
                e.setSellMode("online");
                e.setFirstSeenAt(Instant.now());
                e.setLastSeenAt(Instant.now());
                e.setCurrentlyAvailable(true);
                e.setMinPrice(p.getMinPrice());
                e.setMaxPrice(p.getMaxPrice());
                jpa.save(e);
            });
        }
    }

    @Override @Transactional
    public void markMissingAsUnavailable(List<String> seenIds) {
        Set<String> seen = new HashSet<>(seenIds);
        for (PlanEntity e : jpa.findAll()) {
            if (!seen.contains(e.getProviderId()) && e.isCurrentlyAvailable()) {
                e.setCurrentlyAvailable(false);
                e.setLastSeenAt(Instant.now());
                jpa.save(e);
            }
        }
    }

    @Override
    public List<Plan> findOverlapOnline(Instant start, Instant end) {
        return jpa.findBySellModeAndStartsAtBeforeAndEndsAtAfter("online", end, start)
                .stream().map(mapper::toDomain).toList();
    }
}
