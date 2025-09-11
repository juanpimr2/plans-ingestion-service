package com.fever.challenge.plans.adapters.in.rest;

import com.fever.challenge.plans.adapters.in.rest.dto.EventDto;
import com.fever.challenge.plans.adapters.in.rest.dto.SearchResponseDto;
import com.fever.challenge.plans.application.PlanQueryUseCase;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping
public class PlanController {

    private final PlanQueryUseCase useCase;

    public PlanController(PlanQueryUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/search")
    public SearchResponseDto search(
            @RequestParam("starts_at") @NotNull Instant startsAt,
            @RequestParam("ends_at")   @NotNull Instant endsAt) {

        var plans = useCase.findWithin(startsAt, endsAt);
        var events = plans.stream().map(EventDto::from).toList();
        return SearchResponseDto.of(events);
    }
}
