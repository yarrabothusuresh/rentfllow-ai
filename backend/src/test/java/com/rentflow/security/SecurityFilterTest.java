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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestJwtFactory testJwtFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("PEN-01: Protected API without token returns 401 Unauthorized")
    void testUnauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("PEN-02: Fake role header without token returns 401 Unauthorized")
    void testFakeRoleHeaderWithoutTokenRejected() throws Exception {
        mockMvc.perform(get("/api/bookings")
                .header("X-User-Role", "OWNER")
                .header("X-Tenant-Id", "99999999-9999-9999-9999-999999999999"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PEN-03: Fake tenant header ignored when valid JWT is provided")
    void testFakeTenantHeaderIgnoredWithValidJwt() throws Exception {
        String realTenantId = "11111111-1111-1111-1111-111111111111";
        String spoofedTenantId = "22222222-2222-2222-2222-222222222222";
        String validToken = testJwtFactory.createStaffToken(realTenantId, "OWNER");

        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + validToken)
                .header("X-Tenant-Id", spoofedTenantId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PEN-04: Tampered JWT token returns 401 Unauthorized")
    void testTamperedTokenReturns401() throws Exception {
        String token = testJwtFactory.createStaffToken("tenant-1", "OWNER");
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PEN-05: Expired JWT token returns 401 Unauthorized")
    void testExpiredTokenReturns401() throws Exception {
        String expiredToken = testJwtFactory.createExpiredToken("tenant-1", "OWNER");

        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PEN-06: JWT signed with wrong key returns 401 Unauthorized")
    void testWrongSignatureReturns401() throws Exception {
        String wrongKeyToken = testJwtFactory.createInvalidSignatureToken("tenant-1", "OWNER");

        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + wrongKeyToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PEN-07: Public login endpoint is accessible without token")
    void testLoginEndpointIsPublic() throws Exception {
        Map<String, String> body = Map.of(
                "email", "unknown@rentflow.test",
                "password", "invalid"
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized()) // 401 from service logic, not security filter chain
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("PEN-08: Successful login with demo credentials returns valid JWT and grants API access")
    void testLoginAndAccessProtectedApi() throws Exception {
        Map<String, String> body = Map.of(
                "email", "owner@demo.local",
                "password", "ChangeMe123!"
        );

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("OWNER"))
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> responseMap = objectMapper.readValue(loginResponse, Map.class);
        String token = (String) responseMap.get("accessToken");

        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
