package com.rentflow.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Day33CorsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestJwtFactory testJwtFactory;

    @Test
    @DisplayName("CORS-01: Preflight OPTIONS request from allowed origin returns 200 with proper CORS headers")
    void testPreflightOptionsFromAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/bookings")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }

    @Test
    @DisplayName("CORS-02: Preflight OPTIONS request from untrusted origin is rejected or lacks allow origin header")
    void testPreflightOptionsFromDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/bookings")
                .header("Origin", "https://evil-attacker.example.com")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("CORS-03: Authenticated GET request from allowed origin includes Access-Control-Allow-Origin")
    void testAuthenticatedRequestFromAllowedOrigin() throws Exception {
        String token = testJwtFactory.createStaffToken("tenant-1", "OWNER");

        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + token)
                .header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("CORS-04: Authenticated GET request from untrusted origin does NOT receive allow origin header")
    void testAuthenticatedRequestFromUntrustedOrigin() throws Exception {
        String token = testJwtFactory.createStaffToken("tenant-1", "OWNER");

        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + token)
                .header("Origin", "https://untrusted-site.org"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
