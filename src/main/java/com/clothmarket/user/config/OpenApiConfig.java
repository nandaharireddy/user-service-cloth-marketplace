package com.clothmarket.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger 3.0) configuration for the User Service.
 * Defines API metadata, contact info, and JWT Bearer security schemes for Swagger UI testing.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * Builds and registers the OpenAPI definition bean with JWT security schemes.
     *
     * @return customized OpenAPI metadata specification
     */
    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User & Authentication Service API")
                        .description("REST API for customer and vendor registration, authentication, JWT tokens, and address management in Cloth Marketplace.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Cloth Marketplace Engineering Team")
                                .email("engineering@clothmarket.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your HS256 JWT access token obtained from /auth/login")));
    }
}
