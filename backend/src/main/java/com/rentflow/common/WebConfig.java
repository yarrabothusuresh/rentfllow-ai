package com.rentflow.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for RentFlow AI.
 * Note: CORS is centrally managed via Spring Security's CorsConfigurationSource in SecurityConfig.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final com.rentflow.integration.security.ExternalApiKeyInterceptor externalApiKeyInterceptor;

    public WebConfig(com.rentflow.integration.security.ExternalApiKeyInterceptor externalApiKeyInterceptor) {
        this.externalApiKeyInterceptor = externalApiKeyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(externalApiKeyInterceptor)
                .addPathPatterns("/api/v1/external/**");
    }
}
