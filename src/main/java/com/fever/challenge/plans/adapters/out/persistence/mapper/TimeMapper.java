package com.fever.challenge.plans.adapters.out.persistence.mapper;

import java.time.*;
import java.util.Objects;

public class TimeMapper {
    private static final ZoneId UTC = ZoneId.of("UTC");

    public static LocalDate toDate(Instant instant) {
        return Objects.isNull(instant) ? null : instant.atZone(UTC).toLocalDate();
    }
    public static LocalTime toTime(Instant instant) {
        return Objects.isNull(instant) ? null : instant.atZone(UTC).toLocalTime();
    }
    public static Instant toInstant(LocalDate date, LocalTime time) {
        if (Objects.isNull(date) || time == null) return null;
        return ZonedDateTime.of(LocalDateTime.of(date, time), UTC).toInstant();
    }
}
