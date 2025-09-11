package com.fever.challenge.plans.domain.model;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Zone {
    private String name;
    private String currency;
    private Double price;
    private Integer maxCapacity;
}
