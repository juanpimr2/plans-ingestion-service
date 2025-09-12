package com.fever.challenge.plans.adapters.out.provider;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fever.challenge.plans.adapters.out.provider.xml.ProviderBasePlan;
import com.fever.challenge.plans.adapters.out.provider.xml.ProviderInnerPlan;
import com.fever.challenge.plans.adapters.out.provider.xml.ProviderPlanList;
import com.fever.challenge.plans.adapters.out.provider.xml.ProviderZone;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.ProviderClientPort;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class WebClientProviderClient implements ProviderClientPort {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final Duration CALL_TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient;
    private final XmlMapper xmlMapper = new XmlMapper();

    public WebClientProviderClient(WebClient providerWebClient) {
        this.webClient = providerWebClient;
    }

    @Override
    @Retry(name = "provider")
    public List<Plan> fetchPlans() {
        log.debug("Calling provider to fetch plans...");

        String xml = webClient.get()
                .retrieve()
                .bodyToMono(String.class)
                .timeout(CALL_TIMEOUT)
                .onErrorResume(ex -> {
                    String cause = (ex.getCause() != null) ? ex.getCause().toString() : ex.toString();
                    log.error("HTTP call to provider failed (timeout={}s): {}", CALL_TIMEOUT.toSeconds(), cause);
                    return Mono.error(ex);
                })
                .block();

        if (Objects.isNull(xml) || xml.isBlank()) {
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
                .map(providerPlanList -> providerPlanList.output)
                .map(providerOutput -> providerOutput.basePlans)
                .orElse(List.of());

        log.debug("Parsed basePlans count={}", basePlans.size());

        List<Plan> plans = basePlans.stream()
                .filter(Objects::nonNull)
                .filter(bp -> "online".equalsIgnoreCase(bp.sellMode))
                .filter(bp -> bp.plan != null)
                .map(this::toDomain)
                .toList();

        log.debug("Mapped online plans count={}", plans.size());
        return plans;
    }

    private Plan toDomain(ProviderBasePlan bp) {
        ProviderInnerPlan p = bp.plan; //

        Optional<LocalDateTime> start = parseDateTime(p.planStartDate);
        Optional<LocalDateTime> end = parseDateTime(p.planEndDate);

        Optional<Double> min = minPrice(p.zones);
        Optional<Double> max = maxPrice(p.zones);

        Plan plan = Plan.builder()
                .id(p.planId)
                .title(bp.title)
                .startDate(start.map(LocalDateTime::toLocalDate).orElse(null))
                .startTime(start.map(LocalDateTime::toLocalTime).orElse(null))
                .endDate(end.map(LocalDateTime::toLocalDate).orElse(null))
                .endTime(end.map(LocalDateTime::toLocalTime).orElse(null))
                .minPrice(min.orElse(null))
                .maxPrice(max.orElse(null))
                .build();

        log.info("Mapped plan id={} title={} minPrice={} maxPrice={}",
                plan.getId(), plan.getTitle(), plan.getMinPrice(), plan.getMaxPrice());



        return plan;
    }

    private Optional<LocalDateTime> parseDateTime(String text) {
        return Optional.ofNullable(text)
                .filter(s -> !s.isBlank())
                .map(s -> LocalDateTime.parse(s, FMT));
    }

    private Optional<Double> minPrice(List<ProviderZone> zones) {
        return Optional.ofNullable(zones)
                .stream()
                .flatMap(List::stream)
                .map(z -> z.price)
                .filter(Objects::nonNull)
                .map(Double::valueOf)
                .min(Comparator.naturalOrder());
    }

    private Optional<Double> maxPrice(List<ProviderZone> zones) {
        return Optional.ofNullable(zones)
                .stream()
                .flatMap(List::stream)
                .map(providerZone -> providerZone.price)
                .filter(Objects::nonNull)
                .map(Double::valueOf)
                .max(Comparator.naturalOrder());
    }
}
