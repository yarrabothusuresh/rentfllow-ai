package com.rentflow.aisales.dto;

import com.rentflow.aisales.model.CopilotIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CopilotResponseDTO {

    private UUID conversationId;
    private String publicId;
    private CopilotIntent intent = CopilotIntent.UNKNOWN;
    private String answer;
    private List<CopilotDataBlockDTO> dataBlocks = new ArrayList<>();
    private List<CopilotSourceReferenceDTO> sources = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<CopilotRecommendationDTO> recommendations = new ArrayList<>();
    private List<CopilotActionProposalDTO> proposedActions = new ArrayList<>();
    private List<String> followUpSuggestions = new ArrayList<>();
    private List<ToolCallResultDTO> executedTools = new ArrayList<>();
    private long latencyMs;

    public CopilotResponseDTO() {}

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public CopilotIntent getIntent() { return intent; }
    public void setIntent(CopilotIntent intent) { this.intent = intent; }

    public CopilotIntent getDetectedIntent() { return intent; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public List<CopilotDataBlockDTO> getDataBlocks() { return dataBlocks; }
    public void setDataBlocks(List<CopilotDataBlockDTO> dataBlocks) { this.dataBlocks = dataBlocks; }

    public List<CopilotSourceReferenceDTO> getSources() { return sources; }
    public void setSources(List<CopilotSourceReferenceDTO> sources) { this.sources = sources; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public List<CopilotRecommendationDTO> getRecommendations() { return recommendations; }
    public void setRecommendations(List<CopilotRecommendationDTO> recommendations) { this.recommendations = recommendations; }

    public List<CopilotActionProposalDTO> getProposedActions() { return proposedActions; }
    public void setProposedActions(List<CopilotActionProposalDTO> proposedActions) { this.proposedActions = proposedActions; }
    public List<CopilotActionProposalDTO> getActionProposals() { return proposedActions; }

    public List<String> getFollowUpSuggestions() { return followUpSuggestions; }
    public void setFollowUpSuggestions(List<String> followUpSuggestions) { this.followUpSuggestions = followUpSuggestions; }

    public List<ToolCallResultDTO> getExecutedTools() { return executedTools; }
    public void setExecutedTools(List<ToolCallResultDTO> executedTools) { this.executedTools = executedTools; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

    private String role;
    private boolean deterministic;
    private String explanation;
    private String message;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isDeterministic() { return deterministic; }
    public void setDeterministic(boolean deterministic) { this.deterministic = deterministic; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getMessage() { return message != null ? message : answer; }
    public void setMessage(String message) { this.message = message; if (this.answer == null) this.answer = message; }

    public void setDetectedIntent(CopilotIntent intent) { this.intent = intent; }
    public void setSuggestedPrompts(List<String> prompts) { this.followUpSuggestions = prompts; }
    public List<CopilotSourceReferenceDTO> getSourceReferences() { return sources; }
}
