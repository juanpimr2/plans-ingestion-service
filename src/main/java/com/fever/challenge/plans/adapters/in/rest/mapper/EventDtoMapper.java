package com.fever.challenge.plans.adapters.in.rest.mapper;

import com.fever.challenge.plans.adapters.in.rest.dto.EventDto;
import com.fever.challenge.plans.domain.model.Plan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventDtoMapper {

    @Mapping(target = "start_date", source = "startDate", qualifiedByName = "dateToString")
    @Mapping(target = "start_time", source = "startTime", qualifiedByName = "timeToString")
    @Mapping(target = "end_date",   source = "endDate",   qualifiedByName = "dateToString")
    @Mapping(target = "end_time",   source = "endTime",   qualifiedByName = "timeToString")
    EventDto toDto(Plan plan);

    List<EventDto> toDtoList(List<Plan> plans);

    // Enlazamos las funciones estáticas del helper
    @Named("dateToString")
    static String dateToString(java.time.LocalDate d) { return RestTimeMapper.dateToString(d); }

    @Named("timeToString")
    static String timeToString(java.time.LocalTime t) { return RestTimeMapper.timeToString(t); }
}
