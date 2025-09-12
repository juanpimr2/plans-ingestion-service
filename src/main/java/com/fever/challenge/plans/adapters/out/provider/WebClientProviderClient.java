// src/main/java/com/fever/challenge/plans/adapters/out/provider/WebClientProviderClient.java
package com.fever.challenge.plans.adapters.out.provider;

import com.fever.challenge.plans.adapters.out.provider.dto.ProviderResponseDto;
import com.fever.challenge.plans.adapters.out.provider.mapper.ProviderEventMapper;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.ProviderClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientProviderClient implements ProviderClientPort {

    private final WebClient providerWebClient;
    private final ProviderEventMapper eventMapper;

    @Override
    @CircuitBreaker(name = "provider", fallbackMethod = "fallbackFetch")
    @Retry(name = "provider")
    public List<Plan> fetchPlans() {
        ProviderResponseDto response = providerWebClient
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ProviderResponseDto.class)
                .block();

        List<Plan> plans = Objects.isNull(response)
                ? List.of()
                : response.events().stream().map(eventMapper::toDomain).toList();

        log.debug("Provider returned {} plans", plans.size());
        return plans;
    }

    @SuppressWarnings("unused")
    private List<Plan> fallbackFetch(Throwable t) {
        log.warn("Provider fetch failed; returning empty list (fallback)", t);
        return List.of();
    }
}
