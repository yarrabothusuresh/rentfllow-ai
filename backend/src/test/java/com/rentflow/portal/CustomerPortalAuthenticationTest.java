package com.rentflow.portal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.portal.dto.CustomerLoginRequestDTO;
import com.rentflow.security.JwtService;
import com.rentflow.security.RentFlowPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerPortalAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("PEN-09: Customer portal login returns verified JWT with customer claims")
    void testCustomerPortalLoginReturnsVerifiedJwt() throws Exception {
        CustomerLoginRequestDTO req = new CustomerLoginRequestDTO();
        req.setEmail("customer@abcevents.demo");
        req.setPassword("demo");

        String response = mockMvc.perform(post("/api/portal/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.customerId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> map = objectMapper.readValue(response, Map.class);
        String token = (String) map.get("token");

        // Verify token is genuine and parsed by JwtService
        assertTrue(jwtService.isTokenValid(token));
        RentFlowPrincipal principal = jwtService.extractPrincipal(token);
        assertNotNull(principal);
        assertEquals("CUSTOMER", principal.getRole());
        assertNotNull(principal.getCustomerId());
        assertNotNull(principal.getTenantId());
    }

    @Test
    @DisplayName("PEN-10: Customer portal login fails on invalid password")
    void testCustomerPortalLoginFailsBadPassword() throws Exception {
        CustomerLoginRequestDTO req = new CustomerLoginRequestDTO();
        req.setEmail("customer@abcevents.demo");
        req.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/portal/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
