package com.rentflow.phone.controller;

import com.rentflow.phone.model.*;
import com.rentflow.phone.provider.CallHandoffResult;
import com.rentflow.phone.provider.TelephonyProvider;
import com.rentflow.phone.service.PhoneCallService;
import com.rentflow.security.RateLimitingService;
import com.rentflow.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/phone")
public class PhoneCallController {

    private static final Logger log = LoggerFactory.getLogger(PhoneCallController.class);

    @Autowired
    private PhoneCallService phoneCallService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private List<TelephonyProvider> telephonyProviders;

    private String resolveTenant(String headerTenant) {
        return (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
    }

    private void checkNotCustomer(String role) {
        if ("ROLE_CUSTOMER".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role)) {
            throw new SecurityException("Access Denied: Customers cannot access internal telephony consoles.");
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkNotCustomer(role);
        String tenantId = resolveTenant(headerTenant);
        return ResponseEntity.ok(phoneCallService.getDashboardMetrics(tenantId));
    }

    @GetMapping("/calls")
    public ResponseEntity<Page<PhoneCallSession>> getCalls(
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(required = false) CallStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        checkNotCustomer(role);
        String tenantId = resolveTenant(headerTenant);
        return ResponseEntity.ok(phoneCallService.getCalls(tenantId, status, PageRequest.of(page, size)));
    }

    @GetMapping("/calls/{id}")
    public ResponseEntity<Map<String, Object>> getCallDetail(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkNotCustomer(role);
        String tenantId = resolveTenant(headerTenant);

        PhoneCallSession session = phoneCallService.getCall(tenantId, id)
                .orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        List<PhoneTranscriptSegment> transcripts = phoneCallService.getTranscripts(tenantId, id);

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("call", session);
        detail.put("transcripts", transcripts);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/calls/simulate-inbound")
    public ResponseEntity<?> simulateInboundCall(
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkNotCustomer(role);
        String tenantId = resolveTenant(headerTenant);

        String callerNumber = (body != null && body.containsKey("callerNumber")) ? body.get("callerNumber") : "+1 (555) 234-5678";
        String providerCallId = (body != null && body.containsKey("providerCallId")) ? body.get("providerCallId") : "sim-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            PhoneCallSession session = phoneCallService.startInboundCall(tenantId, providerCallId, callerNumber);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/calls/{id}/utterance")
    public ResponseEntity<?> sendUtterance(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkNotCustomer(role);
        String tenantId = resolveTenant(headerTenant);
        String utterance = body != null ? body.get("utterance") : null;

        if (utterance == null || utterance.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Utterance text is required."));
        }

        try {
            Map<String, Object> turnResult = phoneCallService.processCallerUtterance(tenantId, id, utterance.trim());
            return ResponseEntity.ok(turnResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/calls/{id}/handoff")
    public ResponseEntity<?> requestHandoff(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkNotCustomer(role);
        String tenantId = resolveTenant(headerTenant);

        CallHandoffReason reason = CallHandoffReason.CUSTOMER_REQUEST;
        if (body != null && body.containsKey("reason")) {
            try {
                reason = CallHandoffReason.valueOf(body.get("reason"));
            } catch (Exception ignored) {}
        }

        try {
            CallHandoffResult result = phoneCallService.requestHumanHandoff(tenantId, id, reason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/calls/{id}/end")
    public ResponseEntity<?> endCall(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkNotCustomer(role);
        String tenantId = resolveTenant(headerTenant);

        try {
            phoneCallService.endCall(tenantId, id);
            return ResponseEntity.ok(Map.of("status", "COMPLETED", "callId", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<PhoneTenantSettings> getSettings(
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkNotCustomer(role);
        String tenantId = resolveTenant(headerTenant);
        return ResponseEntity.ok(phoneCallService.getSettings(tenantId));
    }

    @PutMapping("/settings")
    public ResponseEntity<PhoneTenantSettings> updateSettings(
            @RequestBody PhoneTenantSettings updated,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkNotCustomer(role);
        String tenantId = resolveTenant(headerTenant);
        return ResponseEntity.ok(phoneCallService.updateSettings(tenantId, updated));
    }

    @PostMapping("/webhooks/{provider}")
    public ResponseEntity<?> handleProviderWebhook(
            @PathVariable String provider,
            @RequestBody(required = false) String rawPayload,
            @RequestHeader(value = "X-Telephony-Signature", required = false) String signature,
            @RequestHeader(value = "X-Telephony-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant
    ) {
        String tenantId = resolveTenant(headerTenant);

        // Rate limiting check
        if (!rateLimitingService.tryAcquire("phone-webhook:" + tenantId, 120)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", "Rate limit exceeded for telephony webhooks."));
        }

        TelephonyProvider resolvedProvider = null;
        for (TelephonyProvider p : telephonyProviders) {
            if (p.getProviderId().equalsIgnoreCase(provider)) {
                resolvedProvider = p;
                break;
            }
        }

        if (resolvedProvider == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unsupported telephony provider: " + provider));
        }

        // Webhook signature verification
        if (signature != null && !resolvedProvider.validateWebhookSignature(signature, timestamp, rawPayload)) {
            log.warn("Telephony webhook signature verification failed for provider {}", provider);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid webhook signature"));
        }

        return ResponseEntity.ok(Map.of("status", "RECEIVED", "provider", provider));
    }
}
