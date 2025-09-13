package com.fever.challenge.plans.provider;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fever.challenge.plans.adapters.out.provider.WebClientProviderClient;
import com.fever.challenge.plans.adapters.out.provider.mapper.ProviderPlanMapper;
import com.fever.challenge.plans.domain.model.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Cadena real: get() -> accept(APPLICATION_XML) -> retrieve() -> bodyToMono(String)
 * Sin deep stubs, usando tipos raw para evitar capturas de comodines.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class WebClientProviderClientTest {

    private WebClient webClient;
    private WebClient.RequestHeadersUriSpec uriSpec;
    private WebClient.RequestHeadersSpec headersSpec;
    private WebClient.ResponseSpec responseSpec;

    private WebClientProviderClient client;

    @BeforeEach
    void setUp() {
        webClient    = mock(WebClient.class);
        uriSpec      = mock(WebClient.RequestHeadersUriSpec.class);
        headersSpec  = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        ProviderPlanMapper mapper = Mappers.getMapper(ProviderPlanMapper.class);
        XmlMapper xmlMapper = new XmlMapper();

        client = new WebClientProviderClient(webClient, xmlMapper, mapper);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.accept(MediaType.APPLICATION_XML)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void fetchPlans_parsesXml_ok() {
        String xml = """
                <planList version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="planList.xsd">
                  <output>
                    <base_plan base_plan_id="291" sell_mode="online" title="Camela en concierto">
                      <plan plan_start_date="2021-06-30T21:00:00" plan_end_date="2021-06-30T22:00:00" plan_id="291" sell_from="2020-07-01T00:00:00" sell_to="2021-06-30T20:00:00" sold_out="false">
                        <zone zone_id="40" capacity="243" price="20.00" name="Platea" numbered="true"/>
                        <zone zone_id="38" capacity="100" price="15.00" name="Grada 2" numbered="false"/>
                        <zone zone_id="30" capacity="90"  price="30.00" name="A28"    numbered="true"/>
                      </plan>
                    </base_plan>
                  </output>
                </planList>
                """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(xml));

        List<Plan> plans = client.fetchPlans();

        assertThat(plans).hasSize(1);
        Plan p = plans.getFirst();
        assertThat(p.getId()).isEqualTo("291");
        assertThat(p.getTitle()).isEqualTo("Camela en concierto");
        assertThat(p.getMinPrice()).isEqualTo(15.0);
        assertThat(p.getMaxPrice()).isEqualTo(30.0);

        verify(webClient).get();
        verify(uriSpec).accept(MediaType.APPLICATION_XML);
        verify(headersSpec).retrieve();
        verify(responseSpec).bodyToMono(String.class);
    }

    @Test
    void fetchPlans_returnsEmpty_whenProviderReturnsEmptyBody() {
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("   "));

        assertThat(client.fetchPlans()).isEmpty();

        verify(webClient).get();
        verify(uriSpec).accept(MediaType.APPLICATION_XML);
        verify(headersSpec).retrieve();
        verify(responseSpec).bodyToMono(String.class);
    }
}
