package com.rentflow.phone.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "phone_call_sessions")
public class PhoneCallSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String publicId; // e.g. CALL-000001

    @Column(nullable = false)
    private String provider; // e.g. "mock", "twilio"

    private String providerCallId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallDirection direction = CallDirection.INBOUND;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status = CallStatus.RINGING;

    @Column(nullable = false)
    private String callerNumberMasked; // e.g. "+1 (555) ***-4829"

    private UUID customerId;

    private UUID leadId;

    private UUID aiConversationId;

    private String assignedUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallConsentStatus consentStatus = CallConsentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallRecordingStatus recordingStatus = CallRecordingStatus.DISABLED;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant answeredAt;

    private Instant endedAt;

    private Instant handoffAt;

    @Enumerated(EnumType.STRING)
    private CallHandoffReason handoffReason;

    @Column(length = 2000)
    private String summary;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public PhoneCallSession() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderCallId() { return providerCallId; }
    public void setProviderCallId(String providerCallId) { this.providerCallId = providerCallId; }

    public CallDirection getDirection() { return direction; }
    public void setDirection(CallDirection direction) { this.direction = direction; }

    public CallStatus getStatus() { return status; }
    public void setStatus(CallStatus status) { this.status = status; }

    public String getCallerNumberMasked() { return callerNumberMasked; }
    public void setCallerNumberMasked(String callerNumberMasked) { this.callerNumberMasked = callerNumberMasked; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getLeadId() { return leadId; }
    public void setLeadId(UUID leadId) { this.leadId = leadId; }

    public UUID getAiConversationId() { return aiConversationId; }
    public void setAiConversationId(UUID aiConversationId) { this.aiConversationId = aiConversationId; }

    public String getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(String assignedUserId) { this.assignedUserId = assignedUserId; }

    public CallConsentStatus getConsentStatus() { return consentStatus; }
    public void setConsentStatus(CallConsentStatus consentStatus) { this.consentStatus = consentStatus; }

    public CallRecordingStatus getRecordingStatus() { return recordingStatus; }
    public void setRecordingStatus(CallRecordingStatus recordingStatus) { this.recordingStatus = recordingStatus; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Instant answeredAt) { this.answeredAt = answeredAt; }

    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public Instant getHandoffAt() { return handoffAt; }
    public void setHandoffAt(Instant handoffAt) { this.handoffAt = handoffAt; }

    public CallHandoffReason getHandoffReason() { return handoffReason; }
    public void setHandoffReason(CallHandoffReason handoffReason) { this.handoffReason = handoffReason; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
