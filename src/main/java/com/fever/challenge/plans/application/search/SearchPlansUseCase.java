package com.fever.challenge.plans.application.search;

import com.fever.challenge.plans.domain.model.Plan;
import java.time.Instant;
import java.util.List;

public interface SearchPlansUseCase {
    List<Plan> findWithin(Instant startsAt, Instant endsAt);
}
