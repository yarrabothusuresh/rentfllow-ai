package com.rentflow.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * Secret key for signing JWTs. Must be at least 256 bits (32 bytes) for HMAC-SHA256.
     */
    private String secret = "rentflow-ai-development-jwt-secret-key-32bytes-long!";

    /**
     * Access token expiration in seconds (default 3600 = 60 minutes).
     */
    private long accessTokenExpiration = 3600;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        if (secret != null && !secret.isBlank()) {
            this.secret = secret;
        }
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(long accessTokenExpiration) {
        if (accessTokenExpiration > 0) {
            this.accessTokenExpiration = accessTokenExpiration;
        }
    }
}
