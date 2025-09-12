package com.fever.challenge.plans.api;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.adapters.out.persistence.repo.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

@SpringBootTest(
        properties = {
                // Desactiva el warm-up en estos tests (0 ms => no llamará al provider)
                "fever.search.warmup-ms=0"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchEndpointIT {

    @Autowired
    MockMvc mockMvc;
    @Mock
    PlanRepository jpa;

    @BeforeEach
    void cleanDb() {
        jpa.deleteAll();
    }

    @Test
    void search_returns_event_when_overlaps_and_online() throws Exception {
        // Semilla en BBDD: plan online que solapa la ventana de búsqueda
        String providerId = "p-" + UUID.randomUUID();

        PlanEntity planEntity = new PlanEntity();
        planEntity.setProviderId(providerId);
        planEntity.setTitle("Sample");
        planEntity.setSellMode("online");
        planEntity.setStartsAt(Instant.parse("2021-06-30T21:00:00Z"));
        planEntity.setEndsAt(Instant.parse("2021-06-30T22:00:00Z"));
        planEntity.setFirstSeenAt(Instant.now());
        planEntity.setLastSeenAt(Instant.now());
        planEntity.setCurrentlyAvailable(true);
        jpa.save(planEntity);

        mockMvc.perform(get("/search")
                        .param("starts_at", "2021-06-01T00:00:00Z")
                        .param("ends_at",   "2021-07-01T00:00:00Z")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.events").isArray())
                .andExpect(jsonPath("$.events.length()").value(1))
                .andExpect(jsonPath("$.events[0].id").value(providerId))
                .andExpect(jsonPath("$.events[0].title").value("Sample"));
    }
}
