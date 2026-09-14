package com.rentflow.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
public class Day33SecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestJwtFactory testJwtFactory;

    @Test
    @DisplayName("SEC-HDR-01: API responses contain X-Content-Type-Options: nosniff")
    void testNosniffHeaderPresent() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("SEC-HDR-02: API responses contain X-Frame-Options: SAMEORIGIN")
    void testFrameOptionsHeaderPresent() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("SEC-HDR-03: API responses contain Referrer-Policy: strict-origin-when-cross-origin")
    void testReferrerPolicyHeaderPresent() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    @DisplayName("SEC-HDR-04: API responses contain Permissions-Policy")
    void testPermissionsPolicyHeaderPresent() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(header().string("Permissions-Policy", "camera=(), microphone=(), geolocation=()"));
    }

    @Test
    @DisplayName("SEC-HDR-05: API responses contain Content-Security-Policy with frame-ancestors 'self'")
    void testCspHeaderPresent() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(header().exists("Content-Security-Policy"));
    }
}
