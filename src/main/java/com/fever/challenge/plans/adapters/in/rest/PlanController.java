package com.fever.challenge.plans.adapters.in.rest;

import com.fever.challenge.plans.adapters.in.rest.dto.EventDto;
import com.fever.challenge.plans.adapters.in.rest.dto.SearchResponseDto;
import com.fever.challenge.plans.adapters.in.rest.mapper.EventDtoMapper;
import com.fever.challenge.plans.application.orchestration.SearchWithWarmupUseCase;
import com.fever.challenge.plans.domain.model.ErrorCode;
import com.fever.challenge.plans.domain.model.ErrorDescription;
import com.fever.challenge.plans.domain.model.Plan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping
@Tag(name = "Search", description = "Search events within a time window")
public class PlanController {

    private final SearchWithWarmupUseCase useCase;
    private final EventDtoMapper mapper;

    @Value("${fever.search.warmup-ms:400}")
    private long warmupMs;

    public PlanController(SearchWithWarmupUseCase useCase, EventDtoMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search plans",
            description = "Returns online plans overlapping the given [starts_at, ends_at] window. " +
                    "On success, returns data.events. If no plans are found, returns error 404. " +
                    "Bad requests return error 400 via handler. Unexpected failures return 500 via handler."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of plans",
                    content = @Content(schema = @Schema(implementation = SearchResponseDto.class),
                            examples = @ExampleObject(
                                    value = "{\"data\":{\"events\":[{\"id\":\"string\",\"title\":\"string\",\"start_date\":\"string\",\"start_time\":\"string\",\"end_date\":\"string\",\"end_time\":\"string\",\"min_price\":0,\"max_price\":0}]},\"error\":null}"
                            ))),
            @ApiResponse(responseCode = "404", description = "No plans found for the given window",
                    content = @Content(schema = @Schema(implementation = SearchResponseDto.class),
                            examples = @ExampleObject(
                                    value = "{\"data\":null,\"error\":{\"code\":\"404\",\"message\":\"No plans were found for the specified time window.\"}}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = SearchResponseDto.class),
                            examples = @ExampleObject(
                                    value = "{\"data\":null,\"error\":{\"code\":\"400\",\"message\":\"The request was not correctly formed (missing required parameters, wrong types...)\"}}"
                            ))),
            @ApiResponse(responseCode = "500", description = "Generic error",
                    content = @Content(schema = @Schema(implementation = SearchResponseDto.class),
                            examples = @ExampleObject(
                                    value = "{\"data\":null,\"error\":{\"code\":\"500\",\"message\":\"An unexpected error occurred.\"}}"
                            )))
    })
    public ResponseEntity<SearchResponseDto> search(
            @RequestParam("starts_at")
            @NotNull
            @Parameter(description = "Start of the window (ISO-8601 instant)", example = "2021-06-30T20:00:00Z")
            Instant startsAt,

            @RequestParam("ends_at")
            @NotNull
            @Parameter(description = "End of the window (ISO-8601 instant)", example = "2021-06-30T23:00:00Z")
            Instant endsAt
    ) {
        // Validación mínima: provoca 400 a través del handler
        if (startsAt.compareTo(endsAt) >= 0) {
            throw new IllegalArgumentException("starts_at must be before ends_at");
        }

        List<Plan> plans = useCase.execute(startsAt, endsAt, Duration.ofMillis(warmupMs));
        if (Objects.isNull(plans) || plans.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SearchResponseDto.error(ErrorCode.NOT_FOUND, ErrorDescription.NOT_FOUND));
        }

        List<EventDto> events = mapper.toDtoList(plans);
        return ResponseEntity.ok(SearchResponseDto.ok(events));
    }
}
