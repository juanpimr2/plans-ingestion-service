package com.fever.challenge.plans.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "fever.provider")
public class ProviderProperties {
    // getters/setters
    private String baseUrl;
    private Integer connectTimeoutMs = 5000;
    private Integer readTimeoutMs = 5000;

}
