package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadata general que aparece en Swagger UI (título, descripción, versión)
 * y el esquema Bearer JWT que usan los endpoints protegidos.
 * La documentación de cada endpoint puntual va con @Operation en el
 * controller correspondiente.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI demoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TP3 · Catálogo, Favoritos, Listas y Autenticación JWT")
                        .description("Catálogo de productos (consumo de una API externa) + favoritos y listas "
                                + "persistidos en PostgreSQL con JPA/Flyway. /api/favoritos y /api/listas "
                                + "requieren autenticación: obtené un token en /api/auth/login y usalo "
                                + "como Bearer token.")
                        .version("v3"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT devuelto por POST /api/auth/login")));
    }
}
