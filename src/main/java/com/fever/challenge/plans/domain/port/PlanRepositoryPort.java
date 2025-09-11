package com.fever.challenge.plans.domain.port;

import com.fever.challenge.plans.domain.model.Plan;
import java.time.Instant;
import java.util.List;

public interface PlanRepositoryPort {
    void upsertAll(List<Plan> plans);
    boolean hasAny(); // útil para warmup
    List<Plan> findOverlapOnline(Instant start, Instant end);
}
