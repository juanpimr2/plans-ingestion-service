package com.fever.challenge.plans.application.orchestration;

import com.fever.challenge.plans.domain.model.Plan;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface SearchWithWarmupUseCase {
    List<Plan> execute(Instant startsAt, Instant endsAt, Duration warmupBudget);
}
