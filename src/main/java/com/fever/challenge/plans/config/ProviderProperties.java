package com.fever.challenge.plans.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Binds provider HTTP client properties from configuration.
 * Defaults are removed so that values must be provided via application.yml/application-test.yml.
 */
@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "fever.provider")
public class ProviderProperties {

    @NotBlank
    private String baseUrl;

    @NotNull
    private Integer connectTimeoutMs;

    @NotNull
    private Integer readTimeoutMs;
}
