package com.rentflow.calendar.dto;

import com.rentflow.calendar.model.ConflictSeverity;
import com.rentflow.calendar.model.ConflictType;

import java.util.UUID;

public class ConflictDTO {
    private ConflictType type;
    private ConflictSeverity severity;
    private String resource;
    private UUID resourceId;
    private Integer requested;
    private Integer available;
    private String conflictingReference;
    private String message;
    private String suggestedAction;

    public ConflictDTO() {}

    public ConflictDTO(ConflictType type, ConflictSeverity severity, String resource, UUID resourceId,
                       Integer requested, Integer available, String conflictingReference, String message, String suggestedAction) {
        this.type = type;
        this.severity = severity;
        this.resource = resource;
        this.resourceId = resourceId;
        this.requested = requested;
        this.available = available;
        this.conflictingReference = conflictingReference;
        this.message = message;
        this.suggestedAction = suggestedAction;
    }

    public ConflictType getType() { return type; }
    public void setType(ConflictType type) { this.type = type; }

    public ConflictSeverity getSeverity() { return severity; }
    public void setSeverity(ConflictSeverity severity) { this.severity = severity; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public UUID getResourceId() { return resourceId; }
    public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }

    public Integer getRequested() { return requested; }
    public void setRequested(Integer requested) { this.requested = requested; }

    public Integer getAvailable() { return available; }
    public void setAvailable(Integer available) { this.available = available; }

    public String getConflictingReference() { return conflictingReference; }
    public void setConflictingReference(String conflictingReference) { this.conflictingReference = conflictingReference; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }
}
