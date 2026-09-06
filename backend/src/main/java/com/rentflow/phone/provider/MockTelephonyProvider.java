package com.rentflow.phone.provider;

import com.rentflow.aisales.dto.AiSalesChatRequestDTO;
import com.rentflow.aisales.dto.AiSalesChatResponseDTO;
import com.rentflow.aisales.model.AiSalesConversation;
import com.rentflow.aisales.repository.AiSalesConversationRepository;
import com.rentflow.aisales.service.AiSalesAgentService;
import com.rentflow.phone.model.*;
import com.rentflow.phone.repository.PhoneCallSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Component("mockTelephonyProvider")
public class MockTelephonyProvider implements TelephonyProvider {

    private static final Logger log = LoggerFactory.getLogger(MockTelephonyProvider.class);

    @Autowired
    private AiSalesAgentService aiSalesAgentService;

    @Autowired
    private PhoneCallSessionRepository sessionRepository;

    @Autowired
    private AiSalesConversationRepository conversationRepository;

    @Override
    public String getProviderId() {
        return "mock";
    }

    @Override
    public boolean validateWebhookSignature(String signature, String timestamp, String payload) {
        // Mock provider accepts standard test signatures or mock-sig
        if (signature == null || signature.trim().isEmpty()) {
            return false;
        }
        return !"invalid-signature".equalsIgnoreCase(signature);
    }

    @Override
    public PhoneCallSession initializeInboundCall(String tenantId, String providerCallId, String callerNumber) {
        PhoneCallSession session = new PhoneCallSession();
        session.setTenantId(tenantId);
        session.setProvider("mock");
        session.setProviderCallId(providerCallId != null ? providerCallId : "mock-call-" + UUID.randomUUID().toString().substring(0, 8));
        session.setDirection(CallDirection.INBOUND);
        session.setStatus(CallStatus.ANSWERED);
        session.setCallerNumberMasked(maskPhoneNumber(callerNumber));
        session.setConsentStatus(CallConsentStatus.GRANTED);
        session.setStartedAt(Instant.now());
        session.setAnsweredAt(Instant.now());

        long count = sessionRepository.countByTenantId(tenantId) + 1;
        session.setPublicId(String.format("CALL-%06d", count));

        return sessionRepository.save(session);
    }

    @Override
    public CallTurnResult processCallerUtterance(PhoneCallSession session, String utterance) {
        if (utterance == null || utterance.trim().isEmpty()) {
            return CallTurnResult.speech("I'm sorry, I didn't quite catch that. Could you please repeat?");
        }

        String lower = utterance.toLowerCase(Locale.ROOT);

        // 1. Check for human handoff request
        if (lower.contains("human") || lower.contains("representative") || lower.contains("talk to someone")
                || lower.contains("speak to someone") || lower.contains("agent") || lower.contains("operator")
                || lower.contains("real person")) {
            return CallTurnResult.handoff(
                    "I understand. Let me transfer you to our live rental specialist right away.",
                    CallHandoffReason.CUSTOMER_REQUEST.name()
            );
        }

        // 2. Check for sensitive payment/card inputs (Strictly prohibited over phone)
        if (lower.contains("credit card") || lower.contains("cvv") || lower.contains("card number")
                || lower.contains("expiration date") || lower.contains("pay with card")) {
            return CallTurnResult.speech(
                    "For your security, we do not take credit card information or process payments over the phone. " +
                    "I have saved your rental request and our team will email you a secure payment link."
            );
        }

        // 3. Delegate to Day 27 AiSalesAgentService using PHONE channel
        AiSalesChatRequestDTO chatReq = new AiSalesChatRequestDTO();
        chatReq.setMessage(utterance);
        chatReq.setChannel("PHONE");
        if (session.getAiConversationId() != null) {
            chatReq.setConversationId(session.getAiConversationId());
        }

        try {
            AiSalesChatResponseDTO response = aiSalesAgentService.handleMessage(session.getTenantId(), "CUSTOMER", chatReq);

            if (session.getAiConversationId() == null && response.getConversationId() != null) {
                session.setAiConversationId(response.getConversationId());
                sessionRepository.save(session);
            }

            // If lead was created, link it to call session
            if (response.getConversationId() != null) {
                conversationRepository.findById(response.getConversationId()).ifPresent(conv -> {
                    if (conv.getLeadId() != null && session.getLeadId() == null) {
                        session.setLeadId(conv.getLeadId());
                        sessionRepository.save(session);
                    }
                });
            }

            String voiceSpeech = formatVoiceResponse(response.getReplyText());
            CallTurnResult result = CallTurnResult.speech(voiceSpeech);
            result.setInquiryUpdated(response.getInquiry() != null);
            return result;

        } catch (Exception e) {
            log.error("Error processing voice utterance through AI Sales Agent: {}", e.getMessage(), e);
            return CallTurnResult.speech("I've noted that down. What else can I help you with for your event?");
        }
    }

    @Override
    public CallHandoffResult transferCall(PhoneCallSession session, String queueOrNumber, CallHandoffReason reason) {
        session.setStatus(CallStatus.WAITING_FOR_HUMAN);
        session.setHandoffAt(Instant.now());
        session.setHandoffReason(reason != null ? reason : CallHandoffReason.CUSTOMER_REQUEST);
        sessionRepository.save(session);

        String dest = queueOrNumber != null ? queueOrNumber : "+1 (800) 555-0199";
        log.info("Transferred call {} to destination queue/number: {}", session.getPublicId(), dest);
        return new CallHandoffResult(true, dest, "Call transferred successfully to " + dest);
    }

    @Override
    public void endCall(PhoneCallSession session) {
        session.setStatus(CallStatus.COMPLETED);
        session.setEndedAt(Instant.now());
        sessionRepository.save(session);
        log.info("Call {} ended successfully.", session.getPublicId());
    }

    private String maskPhoneNumber(String rawNumber) {
        if (rawNumber == null || rawNumber.length() < 7) {
            return "+1 (555) ***-0199";
        }
        // Keep first few and last 4 characters
        String digits = rawNumber.replaceAll("[^0-9+]", "");
        if (digits.length() > 6) {
            return digits.substring(0, Math.min(6, digits.length() - 4)) + "***" + digits.substring(digits.length() - 4);
        }
        return "+1 (555) ***-" + digits.substring(Math.max(0, digits.length() - 4));
    }

    /**
     * Converts markdown / structured chat text into concise, natural spoken voice responses.
     */
    private String formatVoiceResponse(String text) {
        if (text == null) return "Thank you. What date is your event?";
        String cleaned = text.replaceAll("\\*\\*", "")
                .replaceAll("(?m)^[ \t]*[•\\-*][ \t]+", "")
                .replaceAll("(?m)^#+[ \t]+", "")
                .replaceAll("\n+", " ")
                .trim();
        // Keep voice turns concise (first 2-3 sentences max)
        String[] sentences = cleaned.split("(?<=[.?!])\\s+");
        if (sentences.length > 3) {
            return sentences[0] + " " + sentences[1] + " " + sentences[2];
        }
        return cleaned;
    }
}
