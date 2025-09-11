package com.fever.challenge.plans.adapters.in.rest.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponseDto {
    private Data data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        private List<EventDto> events;
    }

    public static SearchResponseDto of(List<EventDto> events) {
        return SearchResponseDto.builder()
                .data(Data.builder().events(events).build())
                .build();
    }
}
