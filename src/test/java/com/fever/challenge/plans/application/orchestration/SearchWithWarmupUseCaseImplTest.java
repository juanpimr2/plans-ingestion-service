package com.fever.challenge.plans.application.orchestration;

import com.fever.challenge.plans.application.orchestration.impl.SearchWithWarmupUseCaseImpl;
import com.fever.challenge.plans.application.refresh.RefreshPlansUseCase;
import com.fever.challenge.plans.application.search.SearchPlansUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SearchWithWarmupUseCaseImplTest {

    @Test
    void returns_db_results_and_triggers_non_blocking_refresh() {
        SearchPlansUseCase search = mock(SearchPlansUseCase.class);
        RefreshPlansUseCase refresh = mock(RefreshPlansUseCase.class);

        SearchWithWarmupUseCaseImpl uc = new SearchWithWarmupUseCaseImpl(search, refresh);

        Instant s = Instant.parse("2021-06-30T20:00:00Z");
        Instant e = Instant.parse("2021-06-30T23:00:00Z");
        List<Plan> db = List.of(Plan.builder().id("x").title("t").build());

        when(search.findWithin(s, e)).thenReturn(db);

        List<Plan> out = uc.execute(s, e, Duration.ofMillis(0));

        assertThat(out).isEqualTo(db);
        verify(search, atLeast(1)).findWithin(s, e);
        verify(refresh, atLeast(1)).refreshNonBlocking(Duration.ofMillis(0));
    }
}
