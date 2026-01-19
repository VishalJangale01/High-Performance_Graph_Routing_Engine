package com.routing.engine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("High-Performance Routing Engine API")
                        .version("1.0")
                        .description("Implementation of Duan et al. (2025) SSSP Algorithm with Bucket Queue Optimization.")
                        .contact(new Contact()
                                .name("Vishal Jangale")
                                .url("https://github.com/VishalJangale01/")));
    }
}