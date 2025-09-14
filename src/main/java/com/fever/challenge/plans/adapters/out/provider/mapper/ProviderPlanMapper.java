package com.fever.challenge.plans.adapters.out.provider.mapper;

import com.fever.challenge.plans.adapters.out.provider.xml.ProviderBasePlan;
import com.fever.challenge.plans.adapters.out.provider.xml.ProviderInnerPlan;
import com.fever.challenge.plans.adapters.out.provider.xml.ProviderZone;
import com.fever.challenge.plans.domain.model.Plan;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface ProviderPlanMapper {

    // El proveedor manda "2021-06-30T21:00:00" (sin 'Z')
    DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Mapea un base_plan del proveedor a nuestro Plan de dominio.
     * Implementación "default" para evitar fricciones con builder/setters.
     */
    default Plan toDomain(ProviderBasePlan bp) {
        if (bp == null || bp.plan == null) return null;

        ProviderInnerPlan p = bp.plan;

        Optional<LocalDateTime> start = parseDateTime(p.planStartDate);
        Optional<LocalDateTime> end   = parseDateTime(p.planEndDate);

        Optional<Double> min = minPrice(p.zones);
        Optional<Double> max = maxPrice(p.zones);

        return Plan.builder()
                .id(p.planId)
                .title(bp.title)
                .startDate(start.map(LocalDateTime::toLocalDate).orElse(null))
                .startTime(start.map(LocalDateTime::toLocalTime).orElse(null))
                .endDate(end.map(LocalDateTime::toLocalDate).orElse(null))
                .endTime(end.map(LocalDateTime::toLocalTime).orElse(null))
                .minPrice(min.orElse(null))
                .maxPrice(max.orElse(null))
                .build();
    }

    // -------- Helpers --------

    default Optional<LocalDateTime> parseDateTime(String text) {
        return Optional.ofNullable(text)
                .filter(s -> !s.isBlank())
                .map(s -> LocalDateTime.parse(s, FMT));
    }

    default Optional<Double> minPrice(List<ProviderZone> zones) {
        return Optional.ofNullable(zones)
                .stream()
                .flatMap(List::stream)
                .map(providerZone -> providerZone.price)
                .filter(Objects::nonNull)
                .map(Double::valueOf)
                .min(Comparator.naturalOrder());
    }

    default Optional<Double> maxPrice(List<ProviderZone> zones) {
        return Optional.ofNullable(zones)
                .stream()
                .flatMap(List::stream)
                .map(providerZone -> providerZone.price)
                .filter(Objects::nonNull)
                .map(Double::valueOf)
                .max(Comparator.naturalOrder());
    }
}
