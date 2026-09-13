package com.rentflow.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TestJwtFactory {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public TestJwtFactory(JwtService jwtService, JwtProperties jwtProperties) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public String createToken(UUID userId, String tenantId, String email, String role, UUID customerId) {
        return jwtService.generateToken(userId, tenantId, email, role, customerId);
    }

    public String createStaffToken(String tenantId, String role) {
        return jwtService.generateToken(
                UUID.randomUUID(),
                tenantId != null ? tenantId : "99999999-9999-9999-9999-999999999999",
                "test-" + (role != null ? role.toLowerCase() : "user") + "@rentflow.test",
                role != null ? role : "OWNER",
                null
        );
    }

    public String createCustomerToken(String tenantId, UUID customerId) {
        return jwtService.generateToken(
                UUID.randomUUID(),
                tenantId != null ? tenantId : "99999999-9999-9999-9999-999999999999",
                "customer@test.com",
                "CUSTOMER",
                customerId != null ? customerId : UUID.randomUUID()
        );
    }

    public String createExpiredToken(String tenantId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtService.CLAIM_TENANT_ID, tenantId);
        claims.put(JwtService.CLAIM_ROLE, role);
        claims.put(JwtService.CLAIM_EMAIL, "expired@rentflow.test");

        long past = System.currentTimeMillis() - 3600000; // 1 hour ago
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);

        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claims(claims)
                .issuedAt(new Date(past - 3600000))
                .expiration(new Date(past))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }

    public String createInvalidSignatureToken(String tenantId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtService.CLAIM_TENANT_ID, tenantId);
        claims.put(JwtService.CLAIM_ROLE, role);
        claims.put(JwtService.CLAIM_EMAIL, "fake@rentflow.test");

        String wrongSecret = "wrong-secret-key-that-is-at-least-32-bytes-long!";
        byte[] keyBytes = wrongSecret.getBytes(StandardCharsets.UTF_8);

        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }
}
