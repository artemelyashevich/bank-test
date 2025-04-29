package com.elyashevich.bank.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String APP_TITLE = "Bank application";
    private static final String APP_DESCRIPTION = "This is a sample API documentation using Swagger for Bank manager application";
    private static final String APP_VERSION = "1.0";


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .servers(
                List.of(
                    new Server().url("http://localhost:8080")
                )
            )
            .info(
                new Info()
                    .title(APP_TITLE)
                    .description(APP_DESCRIPTION)
                    .version(APP_VERSION)
            )
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT")));
    }
}