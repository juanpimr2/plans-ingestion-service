package com.fever.challenge.plans.adapters.in.rest;

import com.fever.challenge.plans.adapters.in.rest.dto.EventDto;
import com.fever.challenge.plans.adapters.in.rest.dto.SearchResponseDto;

import com.fever.challenge.plans.application.orchestation.SearchWithWarmupUseCase;
import com.fever.challenge.plans.domain.model.Plan;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping
public class PlanController {

    private final SearchWithWarmupUseCase useCase;

    public PlanController(SearchWithWarmupUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/search")
    public SearchResponseDto search(
            @RequestParam("starts_at") @NotNull Instant startsAt,
            @RequestParam("ends_at")   @NotNull Instant endsAt) {

        List<Plan> plans = useCase.execute(startsAt, endsAt, Duration.ofMillis(400));
        List<EventDto> events = plans.stream().map(EventDto::from).toList();
        return SearchResponseDto.of(events);
    }
}
