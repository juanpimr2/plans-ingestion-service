package com.fever.challenge.plans.adapters.in.rest.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EventDto {
    private String id;
    private String title;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private Double minPrice;
    private Double maxPrice;
}
