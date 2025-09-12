package com.fever.challenge.plans.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI plansIngestionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plans Ingestion Service API")
                        .description("Microservice that integrates provider plans into Fever marketplace.")
                        .version("v1")
                        .contact(new Contact().name("Fever Challenge")))
                .addServersItem(new Server().url("http://localhost:8080").description("Local"));
    }

}
