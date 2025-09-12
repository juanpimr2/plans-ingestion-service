package com.fever.challenge.plans.adapters.in.rest.mapper;

import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class RestTimeMapper {

    private RestTimeMapper() {}

    @Named("dateToString")
    public static String dateToString(LocalDate localDate) {
        return Objects.nonNull(localDate) ? localDate.toString() : null; // ISO-8601: 2021-06-30
    }

    @Named("timeToString")
    public static String timeToString(LocalTime localTime) {
        return Objects.nonNull(localTime) ? localTime.truncatedTo(ChronoUnit.MINUTES).toString() : null; // 21:00
    }
}
