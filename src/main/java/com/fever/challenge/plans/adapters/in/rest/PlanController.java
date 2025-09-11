package com.fever.challenge.plans.adapters.in.rest;

import com.fever.challenge.plans.adapters.in.rest.dto.PlanDto;
import com.fever.challenge.plans.application.PlanQueryUseCase;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PlanController {

    private final PlanQueryUseCase useCase;

    public PlanController(PlanQueryUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/plans")
    public List<PlanDto> search(
            @RequestParam("starts_at") Instant startsAt,
            @RequestParam("ends_at") Instant endsAt) {
        return useCase.findWithin(startsAt, endsAt);
    }
}
