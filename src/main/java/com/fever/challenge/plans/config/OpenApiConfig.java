package com.fever.challenge.plans.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Plans Ingestion Service API",
                version = "v1",
                description = "Microservice that integrates provider plans into Fever marketplace.",
                contact = @Contact(name = "Fever Challenge")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local")
        }
)
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI();
    }

    // Redirect cómodo para no ver Petstore al abrir la UI
    @Controller
    static class SwaggerRedirect {
        @GetMapping({"/", "/swagger-ui"})
        public String redirect() {
            return "redirect:/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config";
        }
    }
}
