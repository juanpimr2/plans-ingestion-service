package com.fever.challenge.plans.adapters.in.rest.mapper;

import com.fever.challenge.plans.adapters.in.rest.dto.EventDto;
import com.fever.challenge.plans.domain.model.Plan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * Mapper for converting {@link Plan} domain models to {@link EventDto} data transfer objects.
 * Uses {@link RestTimeMapper} for date and time conversions.
 */
@Mapper(componentModel = "spring", uses = { RestTimeMapper.class })
public interface EventDtoMapper {

    /**
     * Converts a {@link Plan} to an {@link EventDto}.
     *
     * @param plan the plan to convert
     * @return the corresponding EventDto
     */
    @Mapping(target = "id",         source = "id")
    @Mapping(target = "title",      source = "title")
    @Mapping(target = "startDate",  source = "startDate", qualifiedByName = "dateToString")
    @Mapping(target = "startTime",  source = "startTime", qualifiedByName = "timeToString")
    @Mapping(target = "endDate",    source = "endDate",   qualifiedByName = "dateToString")
    @Mapping(target = "endTime",    source = "endTime",   qualifiedByName = "timeToString")
    @Mapping(target = "minPrice",   source = "minPrice")
    @Mapping(target = "maxPrice",   source = "maxPrice")
    EventDto toDto(Plan plan);

    /**
     * Converts a list of {@link Plan} objects to a list of {@link EventDto} objects.
     *
     * @param plans the list of plans to convert
     * @return the corresponding list of EventDto objects
     */
    List<EventDto> toDtoList(List<Plan> plans);
}