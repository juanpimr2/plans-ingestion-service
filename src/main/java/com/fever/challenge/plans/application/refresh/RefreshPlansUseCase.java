package com.fever.challenge.plans.application.refresh;

import java.time.Duration;

public interface RefreshPlansUseCase {
    void refreshNonBlocking(Duration timeBudget);

}
