package com.fever.challenge.plans.adapters.in.rest.dto;

import com.fever.challenge.plans.domain.model.Plan;
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

    public static EventDto from(Plan p) {
        return EventDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .start_date(p.getStartDate() != null ? p.getStartDate().toString() : null)
                .start_time(p.getStartTime() != null ? p.getStartTime().toString() : null)
                .end_date(p.getEndDate() != null ? p.getEndDate().toString() : null)
                .end_time(p.getEndTime() != null ? p.getEndTime().toString() : null)
                .min_price(p.getMinPrice())
                .max_price(p.getMaxPrice())
                .build();
    }
}
