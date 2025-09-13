package com.fever.challenge.plans.provider;

import com.fever.challenge.plans.adapters.out.provider.WebClientProviderClient;
import com.fever.challenge.plans.adapters.out.provider.dto.ProviderEventDto;
import com.fever.challenge.plans.adapters.out.provider.dto.ProviderResponseDto;
import com.fever.challenge.plans.adapters.out.provider.mapper.ProviderEventMapper;
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
 * Test sin RETURNS_DEEP_STUBS y con tipos raw para evitar problemas de comodines (capture of ?).
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class WebClientProviderClientTest {

    private WebClient webClient;
    private WebClient.RequestHeadersUriSpec uriSpec;
    private WebClient.RequestHeadersSpec headersSpec;
    private WebClient.ResponseSpec responseSpec;

    private ProviderEventMapper mapper;
    private WebClientProviderClient client;

    @BeforeEach
    void setUp() {
        webClient   = mock(WebClient.class);
        uriSpec     = mock(WebClient.RequestHeadersUriSpec.class);
        headersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec= mock(WebClient.ResponseSpec.class);

        mapper = Mappers.getMapper(ProviderEventMapper.class);
        client = new WebClientProviderClient(webClient, mapper);

        // Cadena real del cliente: get() -> accept(JSON) -> retrieve() -> bodyToMono(...)
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void fetchPlans_parsesJson_ok() {
        ProviderEventDto dto = new ProviderEventDto(
                "291", "Camela en concierto",
                "2021-06-30", "21:00",
                "2021-06-30", "22:00",
                15.00, 20.00
        );
        ProviderResponseDto response = new ProviderResponseDto(List.of(dto));

        when(responseSpec.bodyToMono(ProviderResponseDto.class))
                .thenReturn(Mono.just(response));

        List<Plan> plans = client.fetchPlans();

        assertThat(plans).hasSize(1);
        Plan p = plans.getFirst();
        assertThat(p.getId()).isEqualTo("291");
        assertThat(p.getTitle()).isEqualTo("Camela en concierto");
        assertThat(p.getMinPrice()).isEqualTo(15.00);
        assertThat(p.getMaxPrice()).isEqualTo(20.00);

        verify(webClient).get();
        verify(uriSpec).accept(MediaType.APPLICATION_JSON);
        verify(headersSpec).retrieve();
        verify(responseSpec).bodyToMono(ProviderResponseDto.class);
    }

    @Test
    void fetchPlans_returnsEmpty_whenProviderReturnsEmptyBody() {
        when(responseSpec.bodyToMono(ProviderResponseDto.class))
                .thenReturn(Mono.empty());

        assertThat(client.fetchPlans()).isEmpty();

        verify(webClient).get();
        verify(uriSpec).accept(MediaType.APPLICATION_JSON);
        verify(headersSpec).retrieve();
        verify(responseSpec).bodyToMono(ProviderResponseDto.class);
    }
}
