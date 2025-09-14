package com.fever.challenge.plans.domain.service;

import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.PlanRepositoryPort;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Service class for handling operations related to plans.
 * Provides methods to query plans based on time windows and specific criteria.
 */
public class PlanService {

    public static final String START_END_REQUIRED = "start/end required";
    public static final String START_MUST_BE_END = "start must be <= end";
    private final PlanRepositoryPort repository;

    public PlanService(PlanRepositoryPort repository) {
        this.repository = repository;
    }

    /**
     * Retrieves plans that overlap with the given time window \[start, end\] and have sell\_mode set to online.
     *
     * @param start the start of the time window (inclusive)
     * @param end the end of the time window (inclusive)
     * @return a list of plans overlapping with the specified window and with sell\_mode online
     * @throws IllegalArgumentException if start or end is null, or if start is after end
     */

    public List<Plan> findWithin(Instant start, Instant end) {
        if (Objects.isNull(start)|| Objects.isNull(end)) throw new IllegalArgumentException(START_END_REQUIRED);
        if (start.isAfter(end)) throw new IllegalArgumentException(START_MUST_BE_END);
        return repository.findOverlapOnline(start, end);
    }
}
