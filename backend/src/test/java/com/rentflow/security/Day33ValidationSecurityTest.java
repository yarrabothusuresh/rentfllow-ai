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
public class Day33ValidationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtFactory testJwtFactory;

    @Test
    @DisplayName("VAL-01: Login request with invalid email format returns 400 with validation errors")
    void testLoginWithInvalidEmail() throws Exception {
        Map<String, String> body = Map.of(
                "email", "not-an-email",
                "password", "secret"
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("VAL-02: Login request with blank fields returns 400 with validation errors")
    void testLoginWithBlankFields() throws Exception {
        Map<String, String> body = Map.of(
                "email", "",
                "password", ""
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("VAL-03: AI Copilot request with message exceeding 4000 chars returns 400")
    void testAiCopilotWithOversizedMessage() throws Exception {
        String token = testJwtFactory.createStaffToken("tenant-1", "OWNER");
        String oversizedMessage = "A".repeat(4005);

        Map<String, String> body = Map.of(
                "message", oversizedMessage
        );

        mockMvc.perform(post("/api/ai/chat")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.message").exists());
    }

    @Test
    @DisplayName("VAL-04: Malformed JSON payload returns structured 400 Bad Request")
    void testMalformedJsonPayload() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid_json: true, }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
