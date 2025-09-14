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
     * Maps a {@link ProviderBasePlan} from the provider to the domain {@link Plan}.
     * <p>
     * Returns {@code null} if the input or its inner plan is {@code null}.
     * Extracts and parses start/end dates, and computes min/max prices from zones.
     *
     * @param basePlan the provider's base plan to map
     * @return the mapped domain {@link Plan}, or {@code null} if input is invalid
     */
    default Plan toDomain(ProviderBasePlan basePlan) {
        if (Objects.isNull(basePlan) || Objects.isNull(basePlan.plan)) {
            return null;
        }

        ProviderInnerPlan innerPlan = basePlan.plan;

        Optional<LocalDateTime> start = parseDateTime(innerPlan.planStartDate);
        Optional<LocalDateTime> end = parseDateTime(innerPlan.planEndDate);

        Optional<Double> minPrice = minPrice(innerPlan.zones);
        Optional<Double> maxPrice = maxPrice(innerPlan.zones);

        return Plan.builder()
                .id(innerPlan.planId)
                .title(basePlan.title)
                .startDate(start.map(LocalDateTime::toLocalDate).orElse(null))
                .startTime(start.map(LocalDateTime::toLocalTime).orElse(null))
                .endDate(end.map(LocalDateTime::toLocalDate).orElse(null))
                .endTime(end.map(LocalDateTime::toLocalTime).orElse(null))
                .minPrice(minPrice.orElse(null))
                .maxPrice(maxPrice.orElse(null))
                .build();
    }
    // -------- Helpers --------

/**
 * Parses a date-time string into a {@link LocalDateTime} using the defined formatter.
 *
 * @param text the date-time string to parse, may be {@code null} or blank
 * @return an {@link Optional} containing the parsed {@link LocalDateTime}, or empty if input is null or blank
 */
default Optional<LocalDateTime> parseDateTime(String text) {
    return Optional.ofNullable(text)
            .filter(s -> !s.isBlank())
            .map(s -> LocalDateTime.parse(s, FMT));
}

/**
 * Calculates the minimum price from a list of {@link ProviderZone} objects.
 *
 * @param zones the list of provider zones, may be {@code null}
 * @return an {@link Optional} containing the minimum price, or empty if no valid prices are found
 */
default Optional<Double> minPrice(List<ProviderZone> zones) {
    return Optional.ofNullable(zones)
            .stream()
            .flatMap(List::stream)
            .map(providerZone -> providerZone.price)
            .filter(Objects::nonNull)
            .map(Double::valueOf)
            .min(Comparator.naturalOrder());
}

/**
 * Calculates the maximum price from a list of {@link ProviderZone} objects.
 *
 * @param zones the list of provider zones, may be {@code null}
 * @return an {@link Optional} containing the maximum price, or empty if no valid prices are found
 */
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
