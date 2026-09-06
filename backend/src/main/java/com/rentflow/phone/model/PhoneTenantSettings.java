package com.rentflow.phone.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "phone_tenant_settings")
public class PhoneTenantSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String tenantId;

    @Column(nullable = false)
    private boolean phoneAiEnabled = true;

    @Column(nullable = false)
    private String provider = "mock";

    @Column(nullable = false)
    private boolean inboundEnabled = true;

    @Column(nullable = false)
    private boolean recordingEnabled = false;

    @Column(nullable = false)
    private boolean transcriptionEnabled = true;

    @Column(nullable = false)
    private int transcriptRetentionDays = 90;

    private String humanHandoffNumber = "+1 (800) 555-0199";

    @Column(length = 1000)
    private String greeting = "Thanks for calling ABC Event Rentals. I'm the automated rental assistant.";

    @Column(length = 1000)
    private String disclosureText = "This call may be recorded or transcribed to assist with your rental request.";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public PhoneTenantSettings() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public boolean isPhoneAiEnabled() { return phoneAiEnabled; }
    public void setPhoneAiEnabled(boolean phoneAiEnabled) { this.phoneAiEnabled = phoneAiEnabled; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public boolean isInboundEnabled() { return inboundEnabled; }
    public void setInboundEnabled(boolean inboundEnabled) { this.inboundEnabled = inboundEnabled; }

    public boolean isRecordingEnabled() { return recordingEnabled; }
    public void setRecordingEnabled(boolean recordingEnabled) { this.recordingEnabled = recordingEnabled; }

    public boolean isTranscriptionEnabled() { return transcriptionEnabled; }
    public void setTranscriptionEnabled(boolean transcriptionEnabled) { this.transcriptionEnabled = transcriptionEnabled; }

    public int getTranscriptRetentionDays() { return transcriptRetentionDays; }
    public void setTranscriptRetentionDays(int transcriptRetentionDays) { this.transcriptRetentionDays = transcriptRetentionDays; }

    public String getHumanHandoffNumber() { return humanHandoffNumber; }
    public void setHumanHandoffNumber(String humanHandoffNumber) { this.humanHandoffNumber = humanHandoffNumber; }

    public String getGreeting() { return greeting; }
    public void setGreeting(String greeting) { this.greeting = greeting; }

    public String getDisclosureText() { return disclosureText; }
    public void setDisclosureText(String disclosureText) { this.disclosureText = disclosureText; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
