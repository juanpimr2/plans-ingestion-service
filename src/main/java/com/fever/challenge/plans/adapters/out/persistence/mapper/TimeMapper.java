package com.fever.challenge.plans.adapters.out.persistence.mapper;

import java.time.*;

public class TimeMapper {
    private static final ZoneId UTC = ZoneId.of("UTC");

    public static LocalDate toDate(Instant instant) {
        return instant == null ? null : instant.atZone(UTC).toLocalDate();
    }
    public static LocalTime toTime(Instant instant) {
        return instant == null ? null : instant.atZone(UTC).toLocalTime();
    }
    public static Instant toInstant(LocalDate date, LocalTime time) {
        if (date == null || time == null) return null;
        return ZonedDateTime.of(LocalDateTime.of(date, time), UTC).toInstant();
    }
}
