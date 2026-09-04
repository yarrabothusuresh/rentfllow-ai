package com.rentflow.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final com.rentflow.integration.security.ExternalApiKeyInterceptor externalApiKeyInterceptor;

    public WebConfig(com.rentflow.integration.security.ExternalApiKeyInterceptor externalApiKeyInterceptor) {
        this.externalApiKeyInterceptor = externalApiKeyInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:4200", "http://localhost:[*]", "http://127.0.0.1:[*]", "*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(externalApiKeyInterceptor)
                .addPathPatterns("/api/v1/external/**");
    }
}
