package com.fever.challenge.plans.adapters.in.rest.mapper;

import com.fever.challenge.plans.adapters.in.rest.dto.EventDto;
import com.fever.challenge.plans.domain.model.Plan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring", uses = { RestTimeMapper.class })
public interface EventDtoMapper {

    // ---- Plan -> EventDto (único) ----
    @Mappings({
            @Mapping(target = "id",         source = "id"),
            @Mapping(target = "title",      source = "title"),
            @Mapping(target = "start_date", source = "startDate", qualifiedByName = "dateToString"),
            @Mapping(target = "start_time", source = "startTime", qualifiedByName = "timeToString"),
            @Mapping(target = "end_date",   source = "endDate",   qualifiedByName = "dateToString"),
            @Mapping(target = "end_time",   source = "endTime",   qualifiedByName = "timeToString"),
            @Mapping(target = "min_price",  source = "minPrice"),
            @Mapping(target = "max_price",  source = "maxPrice")
    })
    EventDto toDto(Plan plan);

    @Mappings({
            @Mapping(target = "id",         source = "id"),
            @Mapping(target = "title",      source = "title"),
            @Mapping(target = "start_date", source = "startDate", qualifiedByName = "dateToString"),
            @Mapping(target = "start_time", source = "startTime", qualifiedByName = "timeToString"),
            @Mapping(target = "end_date",   source = "endDate",   qualifiedByName = "dateToString"),
            @Mapping(target = "end_time",   source = "endTime",   qualifiedByName = "timeToString"),
            @Mapping(target = "min_price",  source = "minPrice"),
            @Mapping(target = "max_price",  source = "maxPrice")
    })
    List<EventDto> toDtoList(List<Plan> plans);
}
