package com.fever.challenge.plans.config;

import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import com.fever.challenge.plans.domain.service.PlanService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {
    @Bean
    public PlanService planService(PlanRepositoryPort repo) {
        return new PlanService(repo);
    }
}
