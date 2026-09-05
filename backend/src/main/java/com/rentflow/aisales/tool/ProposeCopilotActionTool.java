package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.CopilotActionProposalDTO;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.aisales.model.CopilotActionType;
import com.rentflow.aisales.model.CopilotRiskLevel;
import com.rentflow.aisales.service.CopilotActionService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class ProposeCopilotActionTool implements AiSalesTool {

    private final CopilotActionService actionService;

    public ProposeCopilotActionTool(CopilotActionService actionService) {
        this.actionService = actionService;
    }

    @Override
    public String getName() {
        return "proposeCopilotAction";
    }

    @Override
    public String getDescription() {
        return "Creates a server-side action proposal requiring explicit human confirmation before execution.";
    }

    @Override
    public boolean isCustomerVisible() {
        return false;
    }

    @Override
    public boolean isMutating() {
        return false; // Creation of a proposal is safe/reversible; execution requires confirmation
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "OPERATIONS", "WAREHOUSE", "FINANCE");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        String actionTypeStr = request.getStringParam("actionType", "");
        String targetType = request.getStringParam("targetType", "ENTITY");
        String targetId = request.getStringParam("targetId", "N/A");
        String summary = request.getStringParam("summary", "Action proposal");
        String riskLevelStr = request.getStringParam("riskLevel", "MEDIUM");
        String convIdStr = request.getStringParam("conversationId", null);

        CopilotActionType actionType;
        try {
            actionType = CopilotActionType.valueOf(actionTypeStr.toUpperCase());
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Invalid actionType: " + actionTypeStr, true);
        }

        CopilotRiskLevel riskLevel;
        try {
            riskLevel = CopilotRiskLevel.valueOf(riskLevelStr.toUpperCase());
        } catch (Exception e) {
            riskLevel = CopilotRiskLevel.MEDIUM;
        }

        UUID convId = null;
        if (convIdStr != null && !convIdStr.isBlank()) {
            try {
                convId = UUID.fromString(convIdStr);
            } catch (Exception ignored) {}
        }
        if (convId == null) {
            convId = UUID.randomUUID();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) request.getParameters().getOrDefault("payload", Map.of());

        try {
            CopilotActionProposalDTO proposal = actionService.proposeAction(
                    tenantId, convId, userRole, actionType, targetType, targetId, summary, riskLevel, payload
            );

            return ToolCallResultDTO.success(getName(), Map.of(
                    "proposalId", proposal.getProposalId().toString(),
                    "actionType", proposal.getActionType().name(),
                    "targetType", proposal.getTargetType(),
                    "targetId", proposal.getTargetId(),
                    "summary", proposal.getSummary(),
                    "status", proposal.getStatus().name(),
                    "requiresConfirmation", true,
                    "payload", proposal.getPayload()
            ), true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to propose action: " + e.getMessage(), true);
        }
    }
}
