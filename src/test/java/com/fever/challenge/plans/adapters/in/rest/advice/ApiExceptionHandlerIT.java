package com.fever.challenge.plans.adapters.in.rest.advice;

import com.fever.challenge.plans.application.orchestration.SearchWithWarmupUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = { "fever.search.warmup-ms=0" })
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ApiExceptionHandlerIT.TestConfig.class)
class ApiExceptionHandlerIT {

    @Autowired
    MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
        @Bean @Primary
        SearchWithWarmupUseCase failingUseCase() {
            return (Instant s, Instant e, Duration d) -> { throw new RuntimeException("boom"); };
        }
    }

    @Test
    void maps_generic_exception_to_500() throws Exception {
        mockMvc.perform(get("/search")
                        .param("starts_at", "2021-06-01T00:00:00Z")
                        .param("ends_at",   "2021-07-01T00:00:00Z"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("500"));
    }
}
