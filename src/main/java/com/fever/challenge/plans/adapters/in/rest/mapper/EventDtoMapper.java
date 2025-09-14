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
            @Mapping(target = "startDate", source = "startDate", qualifiedByName = "dateToString"),
            @Mapping(target = "startTime", source = "startTime", qualifiedByName = "timeToString"),
            @Mapping(target = "endDate",   source = "endDate",   qualifiedByName = "dateToString"),
            @Mapping(target = "endTime",   source = "endTime",   qualifiedByName = "timeToString"),
            @Mapping(target = "minPrice",  source = "minPrice"),
            @Mapping(target = "maxPrice",  source = "maxPrice")
    })
    EventDto toDto(Plan plan);

    @Mappings({
            @Mapping(target = "id",         source = "id"),
            @Mapping(target = "title",      source = "title"),
            @Mapping(target = "startDate", source = "startDate", qualifiedByName = "dateToString"),
            @Mapping(target = "startTime", source = "startTime", qualifiedByName = "timeToString"),
            @Mapping(target = "endDate",   source = "endDate",   qualifiedByName = "dateToString"),
            @Mapping(target = "endTime",   source = "endTime",   qualifiedByName = "timeToString"),
            @Mapping(target = "minPrice",  source = "minPrice"),
            @Mapping(target = "maxPrice",  source = "maxPrice")
    })
    List<EventDto> toDtoList(List<Plan> plans);
}
