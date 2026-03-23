package com.consultingops.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI userServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ConsultingOps User Service API")
                .description("Identity, consultant, client, project, assignment, and internal directory APIs.")
                .version("v1")
                .contact(new Contact().name("ConsultingOps").email("platform@consultingops.local"))
                .license(new License().name("Internal Portfolio Project")));
    }
}
