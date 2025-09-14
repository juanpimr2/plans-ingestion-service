package com.fever.challenge.plans.adapters.out.persistence.repo;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<PlanEntity, Long> {
    Optional<PlanEntity> findByProviderId(String providerId);
    List<PlanEntity> findBySellModeAndStartsAtBeforeAndEndsAtAfter(String sellMode, Instant end, Instant start);
    /**
     * Indica si ya hay planes persistidos.
     */
    boolean hasAny();
}
