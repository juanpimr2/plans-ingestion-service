package com.fever.challenge.plans.adapters.out.persistence.mapper;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.domain.model.Plan;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class PlanPersistenceMapperTest {

    private final PlanPersistenceMapper mapper = Mappers.getMapper(PlanPersistenceMapper.class);

    @Test
    void maps_domain_to_entity_and_back() {
        Plan domain = Plan.builder()
                .id("prov-1")
                .title("Show")
                .startDate(LocalDate.parse("2021-06-30"))
                .startTime(LocalTime.parse("21:00"))
                .endDate(LocalDate.parse("2021-06-30"))
                .endTime(LocalTime.parse("22:00"))
                .minPrice(10.0)
                .maxPrice(20.0)
                .build();

        PlanEntity entity = mapper.toEntity(domain);
        assertThat(entity.getProviderId()).isEqualTo("prov-1");
        assertThat(entity.getStartsAt()).isNotNull();
        assertThat(entity.getEndsAt()).isNotNull();

        Plan back = mapper.toDomain(entity);
        assertThat(back.getId()).isEqualTo("prov-1");
        assertThat(back.getStartDate()).isEqualTo(domain.getStartDate());
        assertThat(back.getStartTime()).isEqualTo(domain.getStartTime());
        assertThat(back.getEndDate()).isEqualTo(domain.getEndDate());
        assertThat(back.getEndTime()).isEqualTo(domain.getEndTime());
    }
}
