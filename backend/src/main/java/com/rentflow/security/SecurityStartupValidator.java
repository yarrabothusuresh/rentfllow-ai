package com.rentflow.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Validates critical security parameters at startup.
 * In 'prod' profile, fails fast if critical security settings are missing or insecure.
 */
@Component
public class SecurityStartupValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SecurityStartupValidator.class);

    private final Environment environment;

    @Value("${security.jwt.secret:}")
    private String jwtSecret;

    @Value("${security.cors.allowed-origins:}")
    private String corsAllowedOrigins;

    @Value("${ai.provider:mock}")
    private String aiProvider;

    @Value("${ai.api-key:}")
    private String aiApiKey;

    @Value("${ai.enabled:true}")
    private boolean aiEnabled;

    public SecurityStartupValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        if (isProd) {
            log.info("Running security validation for PRODUCTION profile...");

            // 1. JWT Secret Validation
            if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
                throw new IllegalStateException("CRITICAL SECURITY ERROR: JWT_SECRET must be configured in production profile.");
            }
            if (jwtSecret.length() < 32 || "default-secret".equalsIgnoreCase(jwtSecret) || "secret".equalsIgnoreCase(jwtSecret)) {
                throw new IllegalStateException("CRITICAL SECURITY ERROR: JWT_SECRET in production must be high-entropy and at least 32 characters long.");
            }

            // 2. CORS Wildcard with credentials validation
            if (corsAllowedOrigins != null && corsAllowedOrigins.contains("*")) {
                throw new IllegalStateException("CRITICAL SECURITY ERROR: Wildcard CORS origin (*) is strictly prohibited in production with credentials enabled.");
            }

            // 3. AI API Key in Production
            if (aiEnabled && !"mock".equalsIgnoreCase(aiProvider)) {
                if (aiApiKey == null || aiApiKey.trim().isEmpty()) {
                    throw new IllegalStateException("CRITICAL SECURITY ERROR: AI_API_KEY must be provided when AI is enabled with non-mock provider '" + aiProvider + "'.");
                }
            }

            log.info("Production security startup validation PASSED.");
        } else {
            log.debug("Non-production profile active ({}). Strict production security validation skipped.",
                    Arrays.toString(environment.getActiveProfiles()));
        }
    }
}
