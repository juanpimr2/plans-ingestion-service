package com.fever.challenge.plans.adapters.out.provider;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fever.challenge.plans.adapters.out.provider.xml.*;
import com.fever.challenge.plans.domain.model.Plan;
import com.fever.challenge.plans.domain.port.ProviderClientPort;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class WebClientProviderClient implements ProviderClientPort {

    private final WebClient webClient;
    private final XmlMapper xmlMapper = new XmlMapper();

    // Formato del feed: 2021-06-30T21:00:00 (sin zona)
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public WebClientProviderClient(WebClient providerWebClient) {
        this.webClient = providerWebClient;
    }

    @Override
    @TimeLimiter(name = "provider")
    @Retry(name = "provider")
    public List<Plan> fetchPlans() {
        String xml = webClient.get().retrieve().bodyToMono(String.class).block();

        try {
            ProviderPlanList root = xmlMapper.readValue(xml, ProviderPlanList.class);
            if (root == null || root.output == null || root.output.basePlans == null) return List.of();

            List<Plan> out = new ArrayList<>();
            for (ProviderBasePlan bp : root.output.basePlans) {
                if (bp == null || bp.plan == null) continue;
                if (!"online".equalsIgnoreCase(bp.sellMode)) continue; // requisito

                var p = bp.plan;
                Double min = minPrice(p.zones);
                Double max = maxPrice(p.zones);

                out.add(Plan.builder()
                        .id(p.planId)
                        .title(bp.title)
                        .startDate(parseDate(p.planStartDate))
                        .startTime(parseTime(p.planStartDate))
                        .endDate(parseDate(p.planEndDate))
                        .endTime(parseTime(p.planEndDate))
                        .minPrice(min)
                        .maxPrice(max)
                        .build());
            }
            return out;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse provider XML", ex);
        }
    }

    // Helpers
    private static LocalDate parseDate(String ts) {
        if (ts == null || ts.isBlank()) return null;
        return LocalDate.parse(ts, FMT);
    }
    private static LocalTime parseTime(String ts) {
        if (ts == null || ts.isBlank()) return null;
        return LocalTime.parse(ts, FMT);
    }
    private static Double minPrice(List<ProviderZone> zones) {
        if (zones == null || zones.isEmpty()) return null;
        return zones.stream().map(z -> z.price).filter(Objects::nonNull)
                .map(Double::valueOf).min(Double::compareTo).orElse(null);
    }
    private static Double maxPrice(List<ProviderZone> zones) {
        if (zones == null || zones.isEmpty()) return null;
        return zones.stream().map(z -> z.price).filter(Objects::nonNull)
                .map(Double::valueOf).max(Double::compareTo).orElse(null);
    }
}
