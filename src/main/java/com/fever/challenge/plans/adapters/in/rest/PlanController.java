package com.fever.challenge.plans.adapters.in.rest;

import com.fever.challenge.plans.adapters.in.rest.dto.PlanDto;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PlanController {

    @GetMapping("/plans")
    public List<PlanDto> search(
            @RequestParam("starts_at") Instant startsAt,
            @RequestParam("ends_at") Instant endsAt) {
        // Stub inicial: devolver lista vacía
        return List.of();
    }
}