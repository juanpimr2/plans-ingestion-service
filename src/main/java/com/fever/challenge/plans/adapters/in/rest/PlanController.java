package com.fever.challenge.plans.adapters.in.rest;

import com.fever.challenge.plans.adapters.in.rest.dto.EventDto;
import com.fever.challenge.plans.adapters.in.rest.dto.SearchResponseDto;
import com.fever.challenge.plans.adapters.in.rest.mapper.EventDtoMapper;
import com.fever.challenge.plans.application.orchestration.SearchWithWarmupUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping
public class PlanController {

    private final SearchWithWarmupUseCase useCase;
    private final EventDtoMapper eventDtoMapper;
    private final long warmupMs;

    public PlanController(SearchWithWarmupUseCase useCase,
                          EventDtoMapper eventDtoMapper,
                          @Value("${fever.search.warmup-ms:400}") long warmupMs) {
        this.useCase = useCase;
        this.eventDtoMapper = eventDtoMapper;
        this.warmupMs = warmupMs;

    }

    @GetMapping("/search")
    public SearchResponseDto search(
            @RequestParam("starts_at") @NotNull Instant startsAt,
            @RequestParam("ends_at")   @NotNull Instant endsAt) {

        List<Plan> plans = useCase.execute(startsAt, endsAt, Duration.ofMillis(warmupMs));
        List<EventDto> events = eventDtoMapper.toDtoList(plans);
        return SearchResponseDto.of(events);
    }
}
