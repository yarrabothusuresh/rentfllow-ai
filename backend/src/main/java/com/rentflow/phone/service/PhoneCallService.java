package com.rentflow.phone.service;

import com.rentflow.phone.model.*;
import com.rentflow.phone.provider.CallHandoffResult;
import com.rentflow.phone.provider.CallTurnResult;
import com.rentflow.phone.provider.TelephonyProvider;
import com.rentflow.phone.repository.PhoneCallSessionRepository;
import com.rentflow.phone.repository.PhoneTenantSettingsRepository;
import com.rentflow.phone.repository.PhoneTranscriptSegmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class PhoneCallService {

    private static final Logger log = LoggerFactory.getLogger(PhoneCallService.class);

    @Autowired
    private PhoneCallSessionRepository sessionRepository;

    @Autowired
    private PhoneTranscriptSegmentRepository transcriptRepository;

    @Autowired
    private PhoneTenantSettingsRepository settingsRepository;

    @Autowired
    private List<TelephonyProvider> providers;

    public PhoneTenantSettings getSettings(String tenantId) {
        return settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    PhoneTenantSettings s = new PhoneTenantSettings();
                    s.setTenantId(tenantId);
                    return settingsRepository.save(s);
                });
    }

    @Transactional
    public PhoneTenantSettings updateSettings(String tenantId, PhoneTenantSettings updated) {
        PhoneTenantSettings existing = getSettings(tenantId);
        existing.setPhoneAiEnabled(updated.isPhoneAiEnabled());
        existing.setInboundEnabled(updated.isInboundEnabled());
        existing.setRecordingEnabled(updated.isRecordingEnabled());
        existing.setTranscriptionEnabled(updated.isTranscriptionEnabled());
        existing.setTranscriptRetentionDays(updated.getTranscriptRetentionDays());
        existing.setHumanHandoffNumber(updated.getHumanHandoffNumber());
        existing.setGreeting(updated.getGreeting());
        existing.setDisclosureText(updated.getDisclosureText());
        existing.setUpdatedAt(Instant.now());
        return settingsRepository.save(existing);
    }

    private TelephonyProvider resolveProvider(String providerId) {
        for (TelephonyProvider p : providers) {
            if (p.getProviderId().equalsIgnoreCase(providerId)) {
                return p;
            }
        }
        // Default to first provider or mock
        return providers.isEmpty() ? null : providers.get(0);
    }

    @Transactional
    public PhoneCallSession startInboundCall(String tenantId, String providerCallId, String callerNumber) {
        PhoneTenantSettings settings = getSettings(tenantId);
        if (!settings.isPhoneAiEnabled() || !settings.isInboundEnabled()) {
            throw new IllegalStateException("Inbound Phone AI is disabled for this tenant.");
        }

        TelephonyProvider provider = resolveProvider(settings.getProvider());
        if (provider == null) {
            throw new IllegalStateException("Telephony provider '" + settings.getProvider() + "' not available.");
        }

        PhoneCallSession session = provider.initializeInboundCall(tenantId, providerCallId, callerNumber);

        // Record initial system greeting & consent disclosure in transcript
        String greetingText = settings.getGreeting() + " " + settings.getDisclosureText();
        PhoneTranscriptSegment greetingSeg = new PhoneTranscriptSegment(tenantId, session.getId(), CallSpeaker.SYSTEM, greetingText);
        transcriptRepository.save(greetingSeg);

        log.info("Started inbound phone call session {} for tenant {}", session.getPublicId(), tenantId);
        return session;
    }

    @Transactional
    public Map<String, Object> processCallerUtterance(String tenantId, UUID callId, String utterance) {
        PhoneCallSession session = sessionRepository.findByIdAndTenantId(callId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Phone call session not found: " + callId));

        if (session.getStatus() == CallStatus.COMPLETED || session.getStatus() == CallStatus.FAILED) {
            throw new IllegalStateException("Cannot process utterance for closed call: " + session.getStatus());
        }

        PhoneTenantSettings settings = getSettings(tenantId);
        TelephonyProvider provider = resolveProvider(session.getProvider());

        // 1. Record customer utterance
        PhoneTranscriptSegment custSeg = new PhoneTranscriptSegment(tenantId, callId, CallSpeaker.CUSTOMER, utterance);
        transcriptRepository.save(custSeg);

        // 2. Process utterance via provider (delegates to AiSalesAgentService)
        CallTurnResult result = provider.processCallerUtterance(session, utterance);

        // 3. Record AI speech response
        PhoneTranscriptSegment aiSeg = new PhoneTranscriptSegment(tenantId, callId, CallSpeaker.AI, result.getResponseSpeech());
        transcriptRepository.save(aiSeg);

        // 4. Update session status
        if (result.isHandoffRequested()) {
            provider.transferCall(session, settings.getHumanHandoffNumber(), CallHandoffReason.CUSTOMER_REQUEST);
        } else {
            session.setStatus(CallStatus.AI_ACTIVE);
        }
        session.setUpdatedAt(Instant.now());
        sessionRepository.save(session);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("callId", session.getId());
        response.put("publicId", session.getPublicId());
        response.put("status", session.getStatus());
        response.put("aiResponseSpeech", result.getResponseSpeech());
        response.put("handoffRequested", result.isHandoffRequested());
        response.put("leadId", session.getLeadId());
        return response;
    }

    @Transactional
    public CallHandoffResult requestHumanHandoff(String tenantId, UUID callId, CallHandoffReason reason) {
        PhoneCallSession session = sessionRepository.findByIdAndTenantId(callId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Phone call session not found: " + callId));

        PhoneTenantSettings settings = getSettings(tenantId);
        TelephonyProvider provider = resolveProvider(session.getProvider());

        CallHandoffResult result = provider.transferCall(session, settings.getHumanHandoffNumber(), reason);

        PhoneTranscriptSegment handoffSeg = new PhoneTranscriptSegment(
                tenantId, callId, CallSpeaker.SYSTEM,
                "Call transferred to human agent (" + result.getTargetDestination() + ") due to: " + reason
        );
        transcriptRepository.save(handoffSeg);

        return result;
    }

    @Transactional
    public void endCall(String tenantId, UUID callId) {
        PhoneCallSession session = sessionRepository.findByIdAndTenantId(callId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Phone call session not found: " + callId));

        TelephonyProvider provider = resolveProvider(session.getProvider());
        provider.endCall(session);

        PhoneTranscriptSegment endSeg = new PhoneTranscriptSegment(tenantId, callId, CallSpeaker.SYSTEM, "Call session ended.");
        transcriptRepository.save(endSeg);
    }

    public Optional<PhoneCallSession> getCall(String tenantId, UUID callId) {
        return sessionRepository.findByIdAndTenantId(callId, tenantId);
    }

    public Page<PhoneCallSession> getCalls(String tenantId, CallStatus status, Pageable pageable) {
        if (status != null) {
            return sessionRepository.findByTenantIdAndStatusOrderByStartedAtDesc(tenantId, status, pageable);
        }
        return sessionRepository.findByTenantIdOrderByStartedAtDesc(tenantId, pageable);
    }

    public List<PhoneTranscriptSegment> getTranscripts(String tenantId, UUID callId) {
        return transcriptRepository.findByTenantIdAndCallSessionIdOrderByTimestampAsc(tenantId, callId);
    }

    public Map<String, Object> getDashboardMetrics(String tenantId) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);

        metrics.put("callsToday", sessionRepository.countByTenantIdAndStartedAtAfter(tenantId, todayStart));
        metrics.put("activeCalls", sessionRepository.countByTenantIdAndStatus(tenantId, CallStatus.AI_ACTIVE)
                + sessionRepository.countByTenantIdAndStatus(tenantId, CallStatus.ANSWERED));
        metrics.put("waitingForHuman", sessionRepository.countByTenantIdAndStatus(tenantId, CallStatus.WAITING_FOR_HUMAN));
        metrics.put("completedCalls", sessionRepository.countByTenantIdAndStatus(tenantId, CallStatus.COMPLETED));
        metrics.put("failedCalls", sessionRepository.countByTenantIdAndStatus(tenantId, CallStatus.FAILED));
        metrics.put("leadsCreated", sessionRepository.countByTenantIdAndLeadIdIsNotNull(tenantId));
        return metrics;
    }

    @Transactional
    public void enforceRetention(String tenantId) {
        PhoneTenantSettings settings = getSettings(tenantId);
        int retentionDays = settings.getTranscriptRetentionDays();
        if (retentionDays > 0) {
            Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
            transcriptRepository.deleteByTenantIdAndCreatedAtBefore(tenantId, cutoff);
            log.info("Enforced phone transcript retention for tenant {}: purged before {}", tenantId, cutoff);
        }
    }
}
