package com.fever.challenge.plans.adapters.out.provider.dto;

import java.util.List;

public record ProviderResponseDto(
        List<ProviderEventDto> events
) {}
