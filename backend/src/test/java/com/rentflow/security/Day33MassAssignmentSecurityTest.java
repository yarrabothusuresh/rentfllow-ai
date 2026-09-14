package com.rentflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Day33MassAssignmentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestJwtFactory testJwtFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("MASS-01: Customer creation binds to JWT tenant, ignoring client-supplied tenantId in body")
    void testCustomerCreationIgnoresBodyTenantId() throws Exception {
        String realTenantId = "11111111-1111-1111-1111-111111111111";
        String spoofedTenantId = "99999999-9999-9999-9999-999999999999";
        String token = testJwtFactory.createStaffToken(realTenantId, "OWNER");

        Map<String, Object> body = Map.of(
                "tenantId", spoofedTenantId,
                "firstName", "Attacker",
                "lastName", "Customer",
                "email", "injected@security-test.com",
                "phone", "555-999-8888"
        );

        mockMvc.perform(post("/api/customers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(realTenantId)); // Must bind to JWT tenant, not spoofed
    }

    @Test
    @DisplayName("MASS-02: Copilot controller uses JWT role, ignoring fake X-User-Role: OWNER header from customer")
    void testCopilotIgnoresHeaderRoleForCustomer() throws Exception {
        String tenantId = "11111111-1111-1111-1111-111111111111";
        // Create token with CUSTOMER role
        String customerToken = testJwtFactory.createCustomerToken(tenantId, java.util.UUID.randomUUID());

        Map<String, String> body = Map.of(
                "pageContextType", "RENTAL_SUMMARY"
        );

        // Even if client injects X-User-Role: OWNER, SecurityContext has CUSTOMER, which is forbidden from copilot
        mockMvc.perform(post("/api/copilot/conversations")
                .header("Authorization", "Bearer " + customerToken)
                .header("X-User-Role", "OWNER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden()); // Must reject customer access despite header spoofing
    }
}
