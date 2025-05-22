package com.siven.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Attachment Service API")
                        .version("1.0")
                        .description("API for uploading and downloading attachments")
                        .contact(new Contact().name("Your Name").email("you@example.com"))
                );
    }
}

