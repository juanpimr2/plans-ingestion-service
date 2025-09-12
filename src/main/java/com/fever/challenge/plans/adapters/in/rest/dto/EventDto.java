package com.fever.challenge.plans.adapters.in.rest.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {
    private String id;
    private String title;
    private String start_date;
    private String start_time;
    private String end_date;
    private String end_time;
    private Double min_price;
    private Double max_price;
}
