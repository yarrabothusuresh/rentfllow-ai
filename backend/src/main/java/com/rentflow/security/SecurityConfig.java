package com.rentflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantContextFilter tenantContextFilter;
    private final ObjectMapper objectMapper;

    @Value("${security.cors.allowed-origins:http://localhost:4200,http://localhost:3000}")
    private String allowedOriginsConfig;

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Tenant-Id", "X-Requested-With",
                "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization", "Link", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> {
                    headers.frameOptions(frame -> frame.sameOrigin());
                    headers.contentTypeOptions(Customizer.withDefaults());
                    headers.referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
                    headers.permissionsPolicy(permissions -> permissions.policy("camera=(), microphone=(), geolocation=()"));
                    headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                            "default-src 'self'; " +
                            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                            "font-src 'self' https://fonts.gstatic.com data:; " +
                            "img-src 'self' data: https:; " +
                            "connect-src 'self' http://localhost:8080 ws: wss:; " +
                            "frame-ancestors 'self';"
                    ));
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        // Permit preflight OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

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
