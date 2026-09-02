package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.aisales.model.AiEscalation;
import com.rentflow.aisales.model.AiEscalationPriority;
import com.rentflow.aisales.model.AiEscalationReason;
import com.rentflow.aisales.model.AiEscalationStatus;
import com.rentflow.aisales.repository.AiEscalationRepository;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationPriority;
import com.rentflow.notification.service.NotificationService;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("aiSalesEscalateToHumanTool")
public class EscalateToHumanTool implements AiSalesTool {

    private final AiEscalationRepository escalationRepository;
    private final NotificationService notificationService;

    public EscalateToHumanTool(AiEscalationRepository escalationRepository, NotificationService notificationService) {
        this.escalationRepository = escalationRepository;
        this.notificationService = notificationService;
    }

    @Override
    public String getName() {
        return "escalateToHuman";
    }

    @Override
    public String getDescription() {
        return "Hand off conversation to a human sales representative and alert staff via in-app notification.";
    }

    @Override
    public boolean isCustomerVisible() {
        return true;
    }

    @Override
    public boolean isMutating() {
        return true;
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("CUSTOMER", "SALES", "ADMIN", "OWNER", "STAFF");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        Map<String, Object> args = request.getArguments();

        String convIdStr = (String) args.get("conversationId");
        UUID convId = convIdStr != null ? UUID.fromString(convIdStr) : UUID.randomUUID();

        String reasonStr = (String) args.getOrDefault("reason", "CUSTOMER_REQUEST");
        AiEscalationReason reason;
        try {
            reason = AiEscalationReason.valueOf(reasonStr);
        } catch (Exception e) {
            reason = AiEscalationReason.CUSTOMER_REQUEST;
        }

        String summary = (String) args.getOrDefault("summary", "Customer requested human assistance.");

        AiEscalation esc = new AiEscalation();
        esc.setTenantId(tenantId);
        esc.setConversationId(convId);
        esc.setReason(reason);
        esc.setPriority(reason == AiEscalationReason.CUSTOMER_REQUEST ? AiEscalationPriority.HIGH : AiEscalationPriority.MEDIUM);
        esc.setStatus(AiEscalationStatus.OPEN);
        esc.setSummary(summary);
        esc = escalationRepository.save(esc);

        // Notify sales staff via NotificationService
        try {
            NotificationRequestDTO notify = new NotificationRequestDTO();
            notify.setTenantId(tenantId);
            notify.setCustomTitle("AI Sales Escalation: " + reason);
            notify.setCustomMessage(summary);
            notify.setChannel(NotificationChannel.IN_APP);
            notify.setPriority(NotificationPriority.HIGH);
            notify.setReferenceType("AI_SALES_ESCALATION");
            notificationService.sendNotification(notify);
        } catch (Exception ignored) {}

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("escalationId", esc.getId());
        res.put("status", esc.getStatus().name());
        res.put("reason", esc.getReason().name());
        res.put("message", "A human sales specialist has been notified and will take over this conversation.");

        return ToolCallResultDTO.success(getName(), res, false);
    }
}
