package com.rentflow.phone.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "phone_transcript_segments")
public class PhoneTranscriptSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID callSessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallSpeaker speaker;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    private Double confidence;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public PhoneTranscriptSegment() {}

    public PhoneTranscriptSegment(String tenantId, UUID callSessionId, CallSpeaker speaker, String text) {
        this.tenantId = tenantId;
        this.callSessionId = callSessionId;
        this.speaker = speaker;
        this.text = text;
        this.timestamp = Instant.now();
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getCallSessionId() { return callSessionId; }
    public void setCallSessionId(UUID callSessionId) { this.callSessionId = callSessionId; }

    public CallSpeaker getSpeaker() { return speaker; }
    public void setSpeaker(CallSpeaker speaker) { this.speaker = speaker; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
