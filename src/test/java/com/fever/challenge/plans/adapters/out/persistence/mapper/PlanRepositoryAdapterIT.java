package com.fever.challenge.plans.adapters.out.persistence;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.adapters.out.persistence.repo.PlanRepository;
import com.fever.challenge.plans.adapters.out.persistence.repo.impl.PlanRepositoryAdapter;
import com.fever.challenge.plans.domain.model.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PlanRepositoryAdapterIT {

    @Autowired
    PlanRepositoryAdapter adapter;

    @Autowired
    PlanRepository jpa;

    @BeforeEach
    void cleanDb() {
        jpa.deleteAll();
    }

    @Test
    void findOverlapOnline_returns_expected() {
        PlanEntity e = new PlanEntity();
        e.setProviderId("p-" + UUID.randomUUID());
        e.setTitle("Sample");
        e.setSellMode("online");
        e.setStartsAt(Instant.parse("2021-06-30T21:00:00Z"));
        e.setEndsAt(Instant.parse("2021-06-30T22:00:00Z"));
        e.setFirstSeenAt(Instant.now());
        e.setLastSeenAt(Instant.now());
        e.setCurrentlyAvailable(true);
        jpa.save(e);

        List<Plan> r = adapter.findOverlapOnline(
                Instant.parse("2021-06-01T00:00:00Z"),
                Instant.parse("2021-07-01T00:00:00Z")
        );

        assertThat(r).hasSize(1);
        assertThat(r.getFirst().getTitle()).isEqualTo("Sample");
    }
}
