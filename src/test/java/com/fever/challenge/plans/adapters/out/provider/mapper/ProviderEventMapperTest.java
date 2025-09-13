package com.fever.challenge.plans.adapters.out.provider.mapper;

import com.fever.challenge.plans.adapters.out.provider.dto.ProviderEventDto;
import com.fever.challenge.plans.domain.model.Plan;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderEventMapperTest {

    private final ProviderEventMapper mapper = Mappers.getMapper(ProviderEventMapper.class);

    @Test
    void maps_dto_to_domain() {
        ProviderEventDto dto = new ProviderEventDto(
                "291", "Camela", "2021-06-30", "21:00", "2021-06-30", "22:00", 15.0, 20.0
        );
        Plan p = mapper.toDomain(dto);
        assertThat(p.getId()).isEqualTo("291");
        assertThat(p.getTitle()).isEqualTo("Camela");
        assertThat(p.getStartTime().toString()).isEqualTo("21:00");
        assertThat(p.getMaxPrice()).isEqualTo(20.0);
    }

    @Test
    void handles_null_prices() {
        ProviderEventDto dto = new ProviderEventDto(
                "x", "t", "2021-06-30", "21:00", "2021-06-30", "22:00", null, null
        );
        Plan p = mapper.toDomain(dto);
        assertThat(p.getMinPrice()).isEqualTo(0.0);
        assertThat(p.getMaxPrice()).isEqualTo(0.0);
    }
}
