package com.fever.challenge.plans.adapters.out.provider.dto;

public record ProviderEventDto(
        String id,
        String title,
        String start_date,
        String start_time,
        String end_date,
        String end_time,
        Double min_price,
        Double max_price
) {}
