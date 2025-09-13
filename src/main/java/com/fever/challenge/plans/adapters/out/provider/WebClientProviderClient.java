package com.fever.challenge.plans.adapters.out.provider;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fever.challenge.plans.adapters.out.provider.mapper.ProviderPlanMapper;
import com.fever.challenge.plans.adapters.out.provider.xml.ProviderBasePlan;
import com.fever.challenge.plans.adapters.out.provider.xml.ProviderPlanList;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.ProviderClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientProviderClient implements ProviderClientPort {

    @Value("${fever.provider.fetch-timeout-ms:3000}")
    private long fetchTimeoutMs;


    private final WebClient providerWebClient;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ProviderPlanMapper mapper;

    @Override
    @CircuitBreaker(name = "provider", fallbackMethod = "fallbackFetch")
    @Retry(name = "provider")
    public List<Plan> fetchPlans() {

        log.debug("Calling provider to fetch plans (XML)…");
        Duration callTimeout = Duration.ofMillis(fetchTimeoutMs);


        String xml = providerWebClient
                .get()
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(callTimeout)
                .onErrorResume(ex -> {
                    log.error("HTTP call to provider failed (timeout={}s): {}", callTimeout.toSeconds(),
                            ex.getMessage());
                    return Mono.error(ex);
                })
                .block();

        if (xml == null || xml.isBlank()) {
            log.warn("Provider returned empty body.");
            return List.of();
        }
        log.debug("Provider response size={} chars", xml.length());

        final ProviderPlanList root;
        try {
            root = xmlMapper.readValue(xml, ProviderPlanList.class);
        } catch (Exception ex) {
            log.error("Failed to parse provider XML", ex);
            throw new RuntimeException("Failed to parse provider XML", ex);
        }

        List<ProviderBasePlan> basePlans = Optional.ofNullable(root)
                .map(pl -> pl.output)
                .map(out -> out.basePlans)
                .orElse(List.of());

        List<Plan> plans = basePlans.stream()
                .filter(Objects::nonNull)
                .filter(bp -> "online".equalsIgnoreCase(bp.sellMode))
                .filter(bp -> bp.plan != null)
                .map(mapper::toDomain)
                .toList();

        log.debug("Mapped online plans count={}", plans.size());
        return plans;
    }

    @SuppressWarnings("unused")
    private List<Plan> fallbackFetch(Throwable t) {
        log.warn("Provider fetch failed; returning empty list (fallback)", t);
        return List.of();
    }
}
