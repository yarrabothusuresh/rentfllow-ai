package com.rentflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Day33RateLimitingSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService.clearAll();
    }

    @Test
    @DisplayName("RATE-01: Token bucket allows requests within quota and rejects excess")
    void testTokenBucketWithinAndBeyondQuota() {
        String testKey = "test-client-1";
        // Category AUTH_LOGIN has quota of 10 requests
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimitingService.tryAcquire(RateLimitingService.RateLimitCategory.AUTH_LOGIN, testKey),
                    "Request " + (i + 1) + " within quota should be permitted");
        }

        // 11th request must be rejected
        assertFalse(rateLimitingService.tryAcquire(RateLimitingService.RateLimitCategory.AUTH_LOGIN, testKey),
                "11th request exceeding quota must be blocked");
    }

    @Test
    @DisplayName("RATE-02: Distinct IP keys maintain independent token buckets")
    void testIndependentBucketsForDifferentKeys() {
        String ip1 = "192.168.1.100";
        String ip2 = "192.168.1.200";

        for (int i = 0; i < 10; i++) {
            rateLimitingService.tryAcquire(RateLimitingService.RateLimitCategory.AUTH_LOGIN, ip1);
        }
        // ip1 is exhausted
        assertFalse(rateLimitingService.tryAcquire(RateLimitingService.RateLimitCategory.AUTH_LOGIN, ip1));

        // ip2 should still be permitted
        assertTrue(rateLimitingService.tryAcquire(RateLimitingService.RateLimitCategory.AUTH_LOGIN, ip2));
    }

    @Test
    @DisplayName("RATE-03: Login endpoint returns 429 Too Many Requests when IP quota exceeded")
    void testLoginEndpointReturns429WhenRateLimited() throws Exception {
        Map<String, String> body = Map.of(
                "email", "owner@demo.local",
                "password", "WrongPassword123!"
        );
        String json = objectMapper.writeValueAsString(body);
        String clientIp = "10.0.0.99";
        String clientKey = clientIp + ":owner@demo.local";

        // Pre-exhaust quota of 10 requests for this client key instantly
        for (int i = 0; i < 10; i++) {
            rateLimitingService.tryAcquire(RateLimitingService.RateLimitCategory.AUTH_LOGIN, clientKey);
        }

        // Attempt must be blocked by rate limiter with 429
        mockMvc.perform(post("/api/auth/login")
                .header("X-Forwarded-For", clientIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }
}
