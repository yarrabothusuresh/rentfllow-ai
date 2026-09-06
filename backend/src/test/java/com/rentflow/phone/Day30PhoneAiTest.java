package com.rentflow.phone;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.phone.controller.PhoneCallController;
import com.rentflow.phone.model.*;
import com.rentflow.phone.repository.PhoneCallSessionRepository;
import com.rentflow.phone.repository.PhoneTranscriptSegmentRepository;
import com.rentflow.phone.service.PhoneCallService;
import com.rentflow.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class Day30PhoneAiTest {

    private static final String TENANT_ID = "00000000-0000-0000-0000-000000000001";

    @Autowired private PhoneCallService phoneCallService;
    @Autowired private PhoneCallController phoneCallController;
    @Autowired private PhoneCallSessionRepository sessionRepository;
    @Autowired private PhoneTranscriptSegmentRepository transcriptRepository;
    @Autowired private ProductRepository productRepository;

    @BeforeEach
    public void setup() {
        SecurityUtils.setTestTenantId(TENANT_ID);

        // Seed inventory product for voice discovery
        if (productRepository.findByTenantIdAndSkuIgnoreCase(TENANT_ID, "CHAIR-WHITE-FOLD").isEmpty()) {
            Product chair = new Product();
            chair.setTenantId(TENANT_ID);
            chair.setName("White Folding Chair");
            chair.setSku("CHAIR-WHITE-FOLD");
            chair.setRentalPrice(BigDecimal.valueOf(2.50));
            chair.setQuantityOwned(500);
            productRepository.save(chair);
        }
    }

    @Test
    @DisplayName("Inbound call initialization records consent disclosure & system greeting")
    public void testInboundCallInitialization() {
        PhoneCallSession session = phoneCallService.startInboundCall(TENANT_ID, "call-prov-101", "+1 (555) 234-5678");

        assertNotNull(session.getId());
        assertEquals(CallStatus.ANSWERED, session.getStatus());
        assertEquals(CallDirection.INBOUND, session.getDirection());
        assertEquals(CallConsentStatus.GRANTED, session.getConsentStatus());
        assertTrue(session.getPublicId().startsWith("CALL-"));

        List<PhoneTranscriptSegment> transcripts = phoneCallService.getTranscripts(TENANT_ID, session.getId());
        assertFalse(transcripts.isEmpty());
        assertEquals(CallSpeaker.SYSTEM, transcripts.get(0).getSpeaker());
        assertTrue(transcripts.get(0).getText().contains("Thanks for calling ABC Event Rentals"));
        assertTrue(transcripts.get(0).getText().contains("recorded or transcribed"));
    }

    @Test
    @DisplayName("Voice conversational turn collects requirements and checks real product catalog")
    public void testVoiceUtteranceProcessing() {
        PhoneCallSession session = phoneCallService.startInboundCall(TENANT_ID, "call-prov-102", "+1 (555) 888-9999");

        Map<String, Object> turn1 = phoneCallService.processCallerUtterance(
                TENANT_ID, session.getId(),
                "Hi, I am planning an event for 100 people and I need white folding chairs."
        );

        assertNotNull(turn1.get("aiResponseSpeech"));
        String reply = (String) turn1.get("aiResponseSpeech");
        assertFalse(reply.isBlank(), "Phone AI must generate voice reply");
        assertEquals(CallStatus.AI_ACTIVE, turn1.get("status"));

        List<PhoneTranscriptSegment> segments = phoneCallService.getTranscripts(TENANT_ID, session.getId());
        assertEquals(3, segments.size(), "Should have SYSTEM greeting, CUSTOMER utterance, and AI response");
        assertEquals(CallSpeaker.CUSTOMER, segments.get(1).getSpeaker());
        assertEquals(CallSpeaker.AI, segments.get(2).getSpeaker());
    }

    @Test
    @DisplayName("Phone AI transfers immediately to human agent upon caller request")
    public void testHumanHandoffRequest() {
        PhoneCallSession session = phoneCallService.startInboundCall(TENANT_ID, "call-prov-103", "+1 (555) 333-4444");

        Map<String, Object> turn = phoneCallService.processCallerUtterance(
                TENANT_ID, session.getId(),
                "I want to speak with a human representative right now."
        );

        assertTrue((Boolean) turn.get("handoffRequested"));
        PhoneCallSession updated = sessionRepository.findById(session.getId()).orElseThrow();
        assertEquals(CallStatus.WAITING_FOR_HUMAN, updated.getStatus());
        assertNotNull(updated.getHandoffAt());
    }

    @Test
    @DisplayName("Phone AI strictly rejects credit card collection over phone")
    public void testProhibitCreditCardOverPhone() {
        PhoneCallSession session = phoneCallService.startInboundCall(TENANT_ID, "call-prov-104", "+1 (555) 777-1234");

        Map<String, Object> turn = phoneCallService.processCallerUtterance(
                TENANT_ID, session.getId(),
                "Can I give you my credit card number 4111 2222 3333 4444 to pay?"
        );

        String reply = (String) turn.get("aiResponseSpeech");
        assertTrue(reply.toLowerCase().contains("do not take credit card")
                || reply.toLowerCase().contains("payment link"),
                "Phone AI must refuse payment card processing over voice");
    }

    @Test
    @DisplayName("Telephony webhook validates signature and rejects invalid requests")
    public void testTelephonyWebhookSignature() {
        ResponseEntity<?> validResp = phoneCallController.handleProviderWebhook(
                "mock", "{\"event\":\"call.answered\"}", "mock-valid-sig", "1710000000", TENANT_ID
        );
        assertEquals(HttpStatus.OK, validResp.getStatusCode());

        ResponseEntity<?> invalidResp = phoneCallController.handleProviderWebhook(
                "mock", "{\"event\":\"call.answered\"}", "invalid-signature", "1710000000", TENANT_ID
        );
        assertEquals(HttpStatus.UNAUTHORIZED, invalidResp.getStatusCode());
    }
}
