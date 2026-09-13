package com.rentflow.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-must-be-at-least-32-bytes-long-for-hmac-sha256!");
        jwtProperties.setAccessTokenExpiration(3600);
        jwtService = new JwtService(jwtProperties);
    }

    @Test
    @DisplayName("Generate and validate valid JWT token")
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String tenantId = "tenant-123";
        String email = "owner@rentflow.test";
        String role = "OWNER";

        String token = jwtService.generateToken(userId, tenantId, email, role, null);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));

        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(tenantId, jwtService.extractTenantId(token));
        assertEquals(role, jwtService.extractRole(token));
        assertEquals(email, jwtService.extractEmail(token));
        assertNull(jwtService.extractCustomerId(token));
    }

    @Test
    @DisplayName("Generate customer token with customerId claim")
    void testCustomerTokenClaims() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String tenantId = "tenant-456";

        String token = jwtService.generateToken(userId, tenantId, "customer@example.com", "CUSTOMER", customerId);
        assertTrue(jwtService.isTokenValid(token));

        RentFlowPrincipal principal = jwtService.extractPrincipal(token);
        assertNotNull(principal);
        assertEquals(userId, principal.getUserId());
        assertEquals(tenantId, principal.getTenantId());
        assertEquals("CUSTOMER", principal.getRole());
        assertEquals(customerId, principal.getCustomerId());
        assertTrue(principal.isCustomer());
    }

    @Test
    @DisplayName("Reject tampered JWT token")
    void testTamperedTokenRejected() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "tenant-a", "user@test.com", "STAFF", null);

        // Tamper with payload
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        String tamperedToken = parts[0] + "." + parts[1] + "X." + parts[2];

        assertFalse(jwtService.isTokenValid(tamperedToken));
        assertThrows(JwtException.class, () -> jwtService.parseAndValidateToken(tamperedToken));
    }

    @Test
    @DisplayName("Reject token signed with different key")
    void testWrongSecretKeyRejected() {
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("different-secret-key-also-at-least-32-bytes-long-12345");
        JwtService otherService = new JwtService(otherProps);

        String tokenFromOther = otherService.generateToken(UUID.randomUUID(), "tenant-x", "user@test.com", "ADMIN", null);
        assertFalse(jwtService.isTokenValid(tokenFromOther));
    }
}
