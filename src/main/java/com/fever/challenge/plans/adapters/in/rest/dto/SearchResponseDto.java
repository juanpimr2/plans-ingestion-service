package com.fever.challenge.plans.adapters.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fever.challenge.plans.domain.model.ErrorCode;
import com.fever.challenge.plans.domain.model.ErrorDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para la respuesta de búsqueda. Contiene el resultado (data) o un error (error).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponseDto {
    private Data data;
    private Error error;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Data {
        private List<EventDto> events;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Error {
        private String code;
        private String message;
    }

    /** Respuesta de éxito. */
    public static SearchResponseDto ok(List<EventDto> events) {
        return SearchResponseDto.builder()
                .data(Data.builder().events(events).build())
                .build();
    }

    /** Respuesta de error. */
    public static SearchResponseDto error(ErrorCode code, ErrorDescription description) {
        return SearchResponseDto.builder()
                .error(Error.builder()
                        .code(code.getCode())
                        .message(description.getDescription())
                        .build())
                .build();
    }
}
