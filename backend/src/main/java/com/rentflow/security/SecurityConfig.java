package com.rentflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantContextFilter tenantContextFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          TenantContextFilter tenantContextFilter,
                          ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantContextFilter = tenantContextFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> errorBody = new LinkedHashMap<>();
            errorBody.put("status", 401);
            errorBody.put("error", "Unauthorized");
            errorBody.put("message", "Authentication is required");
            errorBody.put("timestamp", Instant.now().toString());
            objectMapper.writeValue(response.getOutputStream(), errorBody);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> errorBody = new LinkedHashMap<>();
            errorBody.put("status", 403);
            errorBody.put("error", "Forbidden");
            errorBody.put("message", "Access is denied");
            errorBody.put("timestamp", Instant.now().toString());
            objectMapper.writeValue(response.getOutputStream(), errorBody);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {}) // Uses existing WebMvcConfigurer or defaults
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // H2 Console support
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/portal/auth/**").permitAll()

                        // Public storefront & rental catalog endpoints
                        .requestMatchers("/api/storefront/**").permitAll()
                        .requestMatchers("/api/rentals/public/**", "/api/rentals/catalog/**", "/api/rentals/cart/**").permitAll()
                        .requestMatchers("/api/cart/**").permitAll()

                        // Public phone webhook endpoints (handled by PhoneCallController / HMAC)
                        .requestMatchers("/api/phone/call/**", "/api/phone/webhook/**").permitAll()

                        // External API v1 endpoints (handled by ExternalApiKeyInterceptor)
                        .requestMatchers("/api/v1/external/**").permitAll()

                        // Dev & infrastructure endpoints
                        .requestMatchers("/h2-console/**", "/error", "/actuator/**").permitAll()

                        // Role boundaries: Staff endpoints forbidden for CUSTOMER role
                        .requestMatchers("/api/admin/**", "/api/warehouse/**", "/api/users/**", "/api/roles/**", "/api/tenants/**")
                            .hasAnyRole("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER")

                        // All other API endpoints must be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantContextFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
