package com.sullung2yo.seatcatcher.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SeatCatcher API")
                        .version("1.0.0")
                        .description("SeatCatcher API Documentation")
                        .contact(new Contact()
                                .name("SeatCatcher Team")
                                .email("sullung2yo@gmail.com")
                                .url("https://github.com/seat-catcher/SeatCatcher-API")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://opensource.org/license/apache-2-0")
                        )
                )
                .servers(List.of(
                        new Server().url("http://api:8080")
                                .description("Docker internal API Server")
                ));
    }
}
