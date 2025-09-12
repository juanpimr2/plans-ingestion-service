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
 * Test adaptado al nuevo WebClientProviderClient:
 * - Usa DTOs JSON (ProviderResponseDto/ProviderEventDto) y Mapper MapStruct.
 * - Mockea bodyToMono(ProviderResponseDto.class) en lugar de String/XML.
 */
class WebClientProviderClientTest {

    private WebClient webClient;                 // deep stub
    private WebClientProviderClient client;

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        // MapStruct mapper
        ProviderEventMapper mapper = Mappers.getMapper(ProviderEventMapper.class);
        client = new WebClientProviderClient(webClient, mapper);
    }

    @Test
    void fetchPlans_parsesJson_ok() {
        // given
        ProviderEventDto dto = new ProviderEventDto(
                "291",
                "Camela en concierto",
                "2021-06-30",
                "21:00",
                "2021-06-30",
                "22:00",
                15.00,
                20.00
        );
        ProviderResponseDto response = new ProviderResponseDto(List.of(dto));

        when(webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ProviderResponseDto.class))
                .thenReturn(Mono.just(response));

        // when
        List<Plan> plans = client.fetchPlans();

        // then
        assertThat(plans).hasSize(1);
        Plan p = plans.getFirst();
        assertThat(p.getId()).isEqualTo("291");
        assertThat(p.getTitle()).isEqualTo("Camela en concierto");
        assertThat(p.getStartDate().toString()).isEqualTo("2021-06-30");
        assertThat(p.getStartTime().toString()).isEqualTo("21:00");
        assertThat(p.getEndDate().toString()).isEqualTo("2021-06-30");
        assertThat(p.getEndTime().toString()).isEqualTo("22:00");
        assertThat(p.getMinPrice()).isEqualTo(15.00);
        assertThat(p.getMaxPrice()).isEqualTo(20.00);

        verify(webClient.get().retrieve(), times(1)).bodyToMono(ProviderResponseDto.class);
    }

    @Test
    void fetchPlans_returnsEmpty_whenProviderReturnsEmptyBody() {
        // given
        when(webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ProviderResponseDto.class))
                .thenReturn(Mono.empty());

        // when / then
        assertThat(client.fetchPlans()).isEmpty();
        verify(webClient.get().retrieve(), times(1)).bodyToMono(ProviderResponseDto.class);
    }
}
