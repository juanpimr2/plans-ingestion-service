package com.fever.challenge.plans.adapters.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
public class SearchResponseDto {
    List<EventDto> events;

    public static SearchResponseDto of(List<EventDto> events) {
        return SearchResponseDto.builder().events(events).build();
    }
}
