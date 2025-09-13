package com.fever.challenge.plans.domain.service;

import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PlanServiceTest {

    @Test
    void delegates_to_repo_and_validates_window() {
        PlanRepositoryPort repo = mock(PlanRepositoryPort.class);
        PlanService service = new PlanService(repo);

        Instant start = Instant.parse("2021-06-30T20:00:00Z");
        Instant end   = Instant.parse("2021-06-30T23:00:00Z");

        service.findWithin(start, end);
        verify(repo, times(1)).findOverlapOnline(start, end);
    }

    @Test
    void throws_when_null_params() {
        PlanRepositoryPort repo = mock(PlanRepositoryPort.class);
        PlanService service = new PlanService(repo);

        assertThatThrownBy(() -> service.findWithin(null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.findWithin(Instant.now(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throws_when_start_after_end() {
        PlanRepositoryPort repo = mock(PlanRepositoryPort.class);
        PlanService service = new PlanService(repo);

        Instant start = Instant.parse("2021-07-01T00:00:00Z");
        Instant end   = Instant.parse("2021-06-30T00:00:00Z");

        assertThatThrownBy(() -> service.findWithin(start, end))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repo);
    }
}
