package com.nexbill.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI nexBillOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NEXBILL SaaS - Multi-Tenant Billing & POS REST API")
                        .description("Production REST API suite for NEXBILL Multi-Tenant SaaS, powering POS billing, inventory, ledger, and business management.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NEXBILL Architecture Team")
                                .email("dev@nexbill.internal")
                                .url("https://nexbill.internal"))
                        .license(new License().name("Proprietary").url("https://nexbill.internal/terms")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token obtained from /api/v1/auth/login")));
    }
}
