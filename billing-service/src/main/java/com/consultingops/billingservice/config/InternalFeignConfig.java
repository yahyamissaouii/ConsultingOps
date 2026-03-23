package com.consultingops.billingservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class InternalFeignConfig {

    @Bean
    RequestInterceptor internalApiKeyInterceptor(@Value("${app.security.internal-api-key}") String apiKey) {
        return template -> template.header("X-Internal-Api-Key", apiKey);
    }
}
