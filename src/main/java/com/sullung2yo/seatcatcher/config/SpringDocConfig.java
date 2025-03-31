package com.sullung2yo.seatcatcher.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String securitySchemeName = "Bearer Authentication";
        String contactUrl = "https://github.com/seat-catcher/SeatCatcher-API";
        String tryOutServerUrl = "https://api.dev.seatcatcher.site";
        return new OpenAPI()
                .info(new Info()
                        .title("SeatCatcher API")
                        .version("1.0.0")
                        .description("SeatCatcher API Documentation")
                        .contact(new Contact()
                                .name("SeatCatcher Team")
                                .email("sullung2yo@gmail.com")
                                .url(contactUrl)
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://opensource.org/license/apache-2-0")
                        )
                )
                .servers(List.of(
                        new Server().url(tryOutServerUrl)
                                .description("Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                );
    }
}
