package com.consultingops.billingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI billingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ConsultingOps Billing Service API")
                .description("Billing period generation, summary retrieval, and billing audit APIs.")
                .version("v1")
                .contact(new Contact().name("ConsultingOps").email("platform@consultingops.local"))
                .license(new License().name("Internal Portfolio Project")));
    }
}
