package com.example.helloswagger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI helloSwaggerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hello Swagger API")
                        .description("Spring Boot demo that returns hello world via Swagger/OpenAPI.")
                        .version("1.0.0"));
    }
}
