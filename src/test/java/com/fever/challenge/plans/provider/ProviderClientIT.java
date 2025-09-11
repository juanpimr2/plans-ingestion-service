package com.fever.challenge.plans.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fever.challenge.plans.adapters.out.provider.WebClientProviderClient;
import com.fever.challenge.plans.domain.model.Plan;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración REAL contra el provider (sin mocks).
 */
@SpringBootTest
@ActiveProfiles("test")
class ProviderClientIT {

    private static final Logger log = LoggerFactory.getLogger(ProviderClientIT.class);


    @Autowired
    private WebClientProviderClient client;

    @Test
    void fetchPlans_from_real_provider_logs_and_maps(){
        log.info("⏩ [IT] Llamada REAL al provider…");
        List<Plan> plans = client.fetchPlans();
        log.info("✅ [IT] Respuesta recibida. Planes mapeados: {}", plans.size());

        assertThat(plans).isNotNull();

        plans.stream().findFirst().ifPresent(p ->
                log.info("🧪 [IT] Primer plan -> id={}, title='{}', minPrice={}, maxPrice={}",
                        p.getId(), p.getTitle(), p.getMinPrice(), p.getMaxPrice())
        );
    }
}
