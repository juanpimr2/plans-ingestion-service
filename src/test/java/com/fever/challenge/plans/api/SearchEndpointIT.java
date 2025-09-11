package com.fever.challenge.plans.api;

import com.fever.challenge.plans.PlansIngestionServiceApplication;
import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.adapters.out.persistence.repo.JpaPlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.fever.challenge.plans.PlansIngestionServiceApplication.class)
@AutoConfigureMockMvc
class SearchEndpointIT {

    @Autowired MockMvc mockMvc;
    @Autowired JpaPlanRepository jpa;

    private static final String START = "2021-06-01T00:00:00Z";
    private static final String END   = "2021-07-01T00:00:00Z";

    @Test
    void search_returns_empty_wrapper_when_no_data() throws Exception {
        mockMvc.perform(get("/search")
                        .param("starts_at", START)
                        .param("ends_at", END)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.events").isArray())
                .andExpect(jsonPath("$.data.events.length()").value(0));
    }

    @Test
    void search_returns_event_when_overlaps_and_online() throws Exception {
        // seed 1 plan that overlaps and is online
        PlanEntity e = PlanEntity.builder()
                .providerId("p-1")
                .title("Sample")
                .sellMode("online")
                .startsAt(Instant.parse("2021-06-15T10:00:00Z"))
                .endsAt(Instant.parse("2021-06-15T12:00:00Z"))
                .minPrice(10.0)
                .maxPrice(20.0)
                .firstSeenAt(Instant.now())
                .lastSeenAt(Instant.now())
                .currentlyAvailable(true)
                .build();
        jpa.save(e);

        mockMvc.perform(get("/search")
                        .param("starts_at", START)
                        .param("ends_at", END)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events.length()").value(1))
                .andExpect(jsonPath("$.data.events[0].id").value("p-1"))
                .andExpect(jsonPath("$.data.events[0].title").value("Sample"))
                .andExpect(jsonPath("$.data.events[0].start_date").value("2021-06-15"))
                .andExpect(jsonPath("$.data.events[0].start_time").value("10:00"))
                .andExpect(jsonPath("$.data.events[0].end_date").value("2021-06-15"))
                .andExpect(jsonPath("$.data.events[0].end_time").value("12:00"))
                .andExpect(jsonPath("$.data.events[0].min_price").value(10.0))
                .andExpect(jsonPath("$.data.events[0].max_price").value(20.0));
    }
}
