package com.fever.challenge.plans.application.refresh;

import com.fever.challenge.plans.domain.model.Plan;

import java.time.Duration;
import java.util.List;

public interface RefreshPlansUseCase {
    void refreshNonBlocking(Duration timeBudget);
    void refreshBlockingUpTo(Duration timeBudget);

    void persistAsync(List<Plan> fresh);
}
