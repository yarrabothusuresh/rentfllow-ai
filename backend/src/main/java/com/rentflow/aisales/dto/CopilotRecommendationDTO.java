package com.rentflow.aisales.dto;

import java.util.ArrayList;
import java.util.List;

public class CopilotRecommendationDTO {

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    private String type;
    private String title;
    private String description;
    private Priority priority;
    private String evidence;
    private List<CopilotSourceReferenceDTO> sourceReferences = new ArrayList<>();
    private String suggestedAction;
    private boolean actionable;

    public CopilotRecommendationDTO() {}

    public CopilotRecommendationDTO(String type, String title, String description, Priority priority, String evidence, String suggestedAction, boolean actionable) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.evidence = evidence;
        this.suggestedAction = suggestedAction;
        this.actionable = actionable;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    public List<CopilotSourceReferenceDTO> getSourceReferences() { return sourceReferences; }
    public void setSourceReferences(List<CopilotSourceReferenceDTO> sourceReferences) { this.sourceReferences = sourceReferences; }

    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }

    public boolean isActionable() { return actionable; }
    public void setActionable(boolean actionable) { this.actionable = actionable; }
}
