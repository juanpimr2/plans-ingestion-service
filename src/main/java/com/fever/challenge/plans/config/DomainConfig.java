package com.fever.challenge.plans.config;

import com.fever.challenge.plans.domain.port.PlanRepositoryPort;
import com.fever.challenge.plans.domain.service.PlanService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DomainConfig {
    @Bean
    public PlanService planService(PlanRepositoryPort planRepository) {
        return new PlanService(planRepository);
    }
}
