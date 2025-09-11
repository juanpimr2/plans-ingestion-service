package com.fever.challenge.plans.domain.port;

import com.fever.challenge.plans.domain.model.Plan;
import java.util.List;

public interface ProviderClientPort {
    List<Plan> fetchPlans();
}
