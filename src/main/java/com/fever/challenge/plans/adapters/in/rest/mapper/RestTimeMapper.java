package com.fever.challenge.plans.adapters.in.rest.mapper;

import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public final class RestTimeMapper {

    private RestTimeMapper() {}

    @Named("dateToString")
    public static String dateToString(LocalDate d) {
        return d != null ? d.toString() : null; // ISO-8601: 2021-06-30
    }

    @Named("timeToString")
    public static String timeToString(LocalTime t) {
        return t != null ? t.truncatedTo(ChronoUnit.MINUTES).toString() : null; // 21:00
    }
}
