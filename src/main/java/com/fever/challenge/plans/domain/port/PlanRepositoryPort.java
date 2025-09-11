package com.fever.challenge.plans.domain.port;

import com.fever.challenge.plans.domain.model.Plan;
import java.time.Instant;
import java.util.List;

public interface PlanRepositoryPort {
    void upsertAll(List<Plan> plans);
    void markMissingAsUnavailable(List<String> seenProviderIds);
    List<Plan> findOverlapOnline(Instant start, Instant end);
}
