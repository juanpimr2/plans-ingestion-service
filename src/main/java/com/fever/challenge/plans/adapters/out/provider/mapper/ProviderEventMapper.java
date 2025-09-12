package com.fever.challenge.plans.adapters.out.provider.mapper;

import com.fever.challenge.plans.adapters.out.provider.dto.ProviderEventDto;
import com.fever.challenge.plans.domain.model.Plan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.LocalTime;

@Mapper(componentModel = "spring")
public interface ProviderEventMapper {

    @Mapping(target = "id",        expression = "java(dto.id())")
    @Mapping(target = "title",     expression = "java(dto.title())")
    @Mapping(target = "startDate", expression = "java(parseDate(dto.start_date()))")
    @Mapping(target = "startTime", expression = "java(parseTime(dto.start_time()))")
    @Mapping(target = "endDate",   expression = "java(parseDate(dto.end_date()))")
    @Mapping(target = "endTime",   expression = "java(parseTime(dto.end_time()))")
    @Mapping(target = "minPrice",  expression = "java(min(dto))")
    @Mapping(target = "maxPrice",  expression = "java(max(dto))")
    Plan toDomain(ProviderEventDto dto);

    default LocalDate parseDate(String d) {
        return (d == null || d.isBlank()) ? null : LocalDate.parse(d);
    }

    default LocalTime parseTime(String t) {
        return (t == null || t.isBlank()) ? null : LocalTime.parse(t);
    }

    default double min(ProviderEventDto dto) {
        return dto.min_price() == null ? 0.0 : dto.min_price();
    }

    default double max(ProviderEventDto dto) {
        return dto.max_price() == null ? min(dto) : dto.max_price();
    }
}
