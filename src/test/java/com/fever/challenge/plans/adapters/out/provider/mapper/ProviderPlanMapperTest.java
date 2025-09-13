package com.fever.challenge.plans.adapters.out.provider.mapper;

import com.fever.challenge.plans.adapters.out.provider.xml.ProviderBasePlan;
import com.fever.challenge.plans.adapters.out.provider.xml.ProviderInnerPlan;
import com.fever.challenge.plans.adapters.out.provider.xml.ProviderZone;
import com.fever.challenge.plans.domain.model.Plan;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderPlanMapperTest {

    private final ProviderPlanMapper mapper = Mappers.getMapper(ProviderPlanMapper.class);

    @Test
    void maps_xml_model_to_domain_ok() {
        // base_plan
        ProviderBasePlan bp = new ProviderBasePlan();
        bp.sellMode = "online";
        bp.title = "Camela en concierto";

        // plan
        ProviderInnerPlan p = new ProviderInnerPlan();
        p.planId = "291";
        p.planStartDate = "2021-06-30T21:00:00";
        p.planEndDate   = "2021-06-30T22:00:00";

        // zones (prices as strings, como en el XML real)
        ProviderZone z1 = new ProviderZone(); z1.price = "20.00";
        ProviderZone z2 = new ProviderZone(); z2.price = "15.00";
        ProviderZone z3 = new ProviderZone(); z3.price = "30.00";
        p.zones = List.of(z1, z2, z3);

        bp.plan = p;

        // when
        Plan plan = mapper.toDomain(bp);

        // then
        assertThat(plan).isNotNull();
        assertThat(plan.getId()).isEqualTo("291");
        assertThat(plan.getTitle()).isEqualTo("Camela en concierto");

        assertThat(plan.getStartDate()).isEqualTo(LocalDate.of(2021, 6, 30));
        assertThat(plan.getStartTime()).isEqualTo(LocalTime.of(21, 0));
        assertThat(plan.getEndDate()).isEqualTo(LocalDate.of(2021, 6, 30));
        assertThat(plan.getEndTime()).isEqualTo(LocalTime.of(22, 0));

        // min/max price calculados desde zones
        assertThat(plan.getMinPrice()).isEqualTo(15.0);
        assertThat(plan.getMaxPrice()).isEqualTo(30.0);
    }

    @Test
    void returns_null_prices_when_no_zones_or_prices() {
        ProviderBasePlan bp = new ProviderBasePlan();
        bp.sellMode = "online";
        bp.title = "Evento sin precios";

        ProviderInnerPlan p = new ProviderInnerPlan();
        p.planId = "X1";
        p.planStartDate = "2021-01-01T10:00:00";
        p.planEndDate   = "2021-01-01T11:00:00";
        // sin zonas (o podrías usar zonas con price = null)
        p.zones = List.of();
        bp.plan = p;

        Plan plan = mapper.toDomain(bp);

        assertThat(plan).isNotNull();
        assertThat(plan.getMinPrice()).isNull();
        assertThat(plan.getMaxPrice()).isNull();
    }
}
