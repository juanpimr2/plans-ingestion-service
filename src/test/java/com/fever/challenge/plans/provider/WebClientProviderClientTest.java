package com.fever.challenge.plans.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fever.challenge.plans.adapters.out.provider.WebClientProviderClient;
import com.fever.challenge.plans.domain.model.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WebClientProviderClientTest {

    public static final String PLAN = """
            <planList version="1.0">
              <output>
                <base_plan base_plan_id="291" sell_mode="online" title="Camela en concierto">
                  <plan plan_start_date="2021-06-30T21:00:00" plan_end_date="2021-06-30T22:00:00"
                        plan_id="291" sell_from="2020-07-01T00:00:00" sell_to="2021-06-30T20:00:00" sold_out="false">
                    <zone zone_id="40" capacity="240" price="20.00" name="Platea" numbered="true"/>
                    <zone zone_id="38" capacity="50"  price="15.00" name="Grada 2" numbered="false"/>
                  </plan>
                </base_plan>
              </output>
            </planList>
            """;
    public static final String PLAN_TITLE = "Camela en concierto";
    public static final String PLANID = "291";
    public static final double MINPRICE = 15.00;
    public static final double MAXPRICE = 20.00;
    public static final String SHOULD_NOT_SHOW = """
            <planList version="1.0">
              <output>
                <base_plan base_plan_id="1" sell_mode="offline" title="No debería aparecer">
                  <plan plan_start_date="2021-06-30T21:00:00" plan_end_date="2021-06-30T22:00:00" plan_id="1"/>
                </base_plan>
              </output>
            </planList>
            """;
    private WebClient webClient;           // deep stub
    private WebClientProviderClient client;

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        client = new WebClientProviderClient(webClient);
    }

    @Test
    void fetchPlans_parsesXml_ok() throws JsonProcessingException {

        when(webClient.get().retrieve().bodyToMono(String.class))
                .thenReturn(Mono.just(PLAN));

        List<Plan> plans = client.fetchPlans();

        assertThat(plans).hasSize(1);
        Plan p = plans.getFirst();
        assertThat(p.getId()).isEqualTo(PLANID);
        assertThat(p.getTitle()).isEqualTo(PLAN_TITLE);
        assertThat(p.getMinPrice()).isEqualTo(MINPRICE);
        assertThat(p.getMaxPrice()).isEqualTo(MAXPRICE);

        verify(webClient.get().retrieve()).bodyToMono(String.class);
    }

    @Test
    void fetchPlans_filters_offline(){

        when(webClient.get().retrieve().bodyToMono(String.class))
                .thenReturn(Mono.just(SHOULD_NOT_SHOW));

        assertThat(client.fetchPlans()).isEmpty();
    }
}
