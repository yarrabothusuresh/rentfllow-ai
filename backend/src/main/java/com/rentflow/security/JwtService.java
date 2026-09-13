package com.rentflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_CUSTOMER_ID = "customerId";

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Pad safely for short dev secrets if necessary, though properties default to >= 32 bytes
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT with standard and custom claims.
     */
    public String generateToken(UUID userId, String tenantId, String email, String role, UUID customerId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TENANT_ID, tenantId);
        claims.put(CLAIM_ROLE, role != null ? role.toUpperCase() : "STAFF");
        if (email != null && !email.isBlank()) {
            claims.put(CLAIM_EMAIL, email.trim().toLowerCase());
        }
        if (customerId != null) {
            claims.put(CLAIM_CUSTOMER_ID, customerId.toString());
        }

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(nowMillis + (jwtProperties.getAccessTokenExpiration() * 1000));

        return Jwts.builder()
                .subject(userId.toString())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Parses and verifies the JWT signature and expiration.
     * @return parsed Claims if valid, throws JwtException if invalid or expired.
     */
    public Claims parseAndValidateToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if a token is valid (signature correct and not expired).
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseAndValidateToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = parseAndValidateToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extractTenantId(String token) {
        Claims claims = parseAndValidateToken(token);
        return claims.get(CLAIM_TENANT_ID, String.class);
    }

    public String extractRole(String token) {
        Claims claims = parseAndValidateToken(token);
        return claims.get(CLAIM_ROLE, String.class);
    }

    public String extractEmail(String token) {
        Claims claims = parseAndValidateToken(token);
        return claims.get(CLAIM_EMAIL, String.class);
    }

    public UUID extractCustomerId(String token) {
        Claims claims = parseAndValidateToken(token);
        String custIdStr = claims.get(CLAIM_CUSTOMER_ID, String.class);
        return custIdStr != null && !custIdStr.isBlank() ? UUID.fromString(custIdStr) : null;
    }

    public RentFlowPrincipal extractPrincipal(String token) {
        Claims claims = parseAndValidateToken(token);
        UUID userId = UUID.fromString(claims.getSubject());
        String tenantId = claims.get(CLAIM_TENANT_ID, String.class);
        String email = claims.get(CLAIM_EMAIL, String.class);
        String role = claims.get(CLAIM_ROLE, String.class);
        String custIdStr = claims.get(CLAIM_CUSTOMER_ID, String.class);
        UUID customerId = (custIdStr != null && !custIdStr.isBlank()) ? UUID.fromString(custIdStr) : null;

        return new RentFlowPrincipal(userId, tenantId, email != null ? email : "", role, customerId);
    }

    public long getExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration();
    }
}
