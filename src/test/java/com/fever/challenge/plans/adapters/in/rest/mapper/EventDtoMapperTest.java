package com.fever.challenge.plans.adapters.in.rest.mapper;

import com.fever.challenge.plans.adapters.in.rest.dto.EventDto;
import com.fever.challenge.plans.domain.model.Plan;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventDtoMapperTest {

    private final EventDtoMapper mapper = Mappers.getMapper(EventDtoMapper.class);

    @Test
    void maps_domain_to_dto() {
        Plan plan = Plan.builder()
                .id("1")
                .title("t")
                .startDate(LocalDate.parse("2021-06-30"))
                .startTime(LocalTime.parse("21:00"))
                .endDate(LocalDate.parse("2021-06-30"))
                .endTime(LocalTime.parse("22:00"))
                .minPrice(10.0)
                .maxPrice(20.0)
                .build();

        EventDto dto = mapper.toDto(plan);
        assertThat(dto.getId()).isEqualTo("1");
        assertThat(dto.getStartDate()).isEqualTo("2021-06-30");
        assertThat(dto.getStartTime()).isEqualTo("21:00");
        assertThat(dto.getMaxPrice()).isEqualTo(20.0);
    }
}
