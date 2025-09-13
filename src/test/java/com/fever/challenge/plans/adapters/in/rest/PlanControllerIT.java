package com.fever.challenge.plans.adapters.in.rest;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.adapters.out.persistence.repo.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = { "fever.search.warmup-ms=0" })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlanControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PlanRepository jpa;

    @BeforeEach
    void cleanDb() {
        jpa.deleteAll();
    }

    @Test
    void returns_200_with_events_when_overlap_and_online() throws Exception {
        String providerId = "p-" + UUID.randomUUID();

        PlanEntity e = new PlanEntity();
        e.setProviderId(providerId);
        e.setTitle("Sample");
        e.setSellMode("online");
        e.setStartsAt(Instant.parse("2021-06-30T21:00:00Z"));
        e.setEndsAt(Instant.parse("2021-06-30T22:00:00Z"));
        e.setFirstSeenAt(Instant.now());
        e.setLastSeenAt(Instant.now());
        e.setCurrentlyAvailable(true);
        jpa.save(e);

        mockMvc.perform(get("/search")
                        .param("starts_at", "2021-06-01T00:00:00Z")
                        .param("ends_at",   "2021-07-01T00:00:00Z")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.events").isArray())
                .andExpect(jsonPath("$.data.events.length()").value(1))
                .andExpect(jsonPath("$.data.events[0].id").value(providerId))
                .andExpect(jsonPath("$.data.events[0].title").value("Sample"));
    }

    @Test
    void returns_404_when_no_events() throws Exception {
        mockMvc.perform(get("/search")
                        .param("starts_at", "2021-06-01T00:00:00Z")
                        .param("ends_at",   "2021-06-02T00:00:00Z")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("404"));
    }

    @Test
    void returns_400_when_invalid_window() throws Exception {
        mockMvc.perform(get("/search")
                        .param("starts_at", "2021-07-01T00:00:00Z")
                        .param("ends_at",   "2021-06-01T00:00:00Z")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("400"));
    }
}
