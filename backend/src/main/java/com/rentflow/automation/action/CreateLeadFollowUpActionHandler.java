package com.rentflow.automation.action;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.crm.model.FollowUpStatus;
import com.rentflow.crm.model.FollowUpType;
import com.rentflow.crm.model.Lead;
import com.rentflow.crm.model.LeadFollowUp;
import com.rentflow.crm.repository.LeadFollowUpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class CreateLeadFollowUpActionHandler implements AutomationActionHandler {

    @Autowired
    @Qualifier("crmLeadRepository")
    private com.rentflow.crm.repository.LeadRepository leadRepository;

    @Autowired
    private LeadFollowUpRepository leadFollowUpRepository;

    @Override
    public AutomationActionType getActionType() {
        return AutomationActionType.CREATE_LEAD_FOLLOW_UP;
    }

    @Override
    public RevalidationResult revalidate(String tenantId, Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("leadId")) {
            return RevalidationResult.stale("Missing leadId in action payload");
        }
        UUID leadId;
        try {
            leadId = UUID.fromString(payload.get("leadId").toString());
        } catch (Exception e) {
            return RevalidationResult.stale("Invalid leadId: " + payload.get("leadId"));
        }

        Lead lead = leadRepository.findByTenantIdAndId(tenantId, leadId).orElse(null);
        if (lead == null) {
            return RevalidationResult.stale("Lead not found or belongs to another tenant");
        }

        return RevalidationResult.valid();
    }

    @Override
    public ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy) {
        UUID leadId = UUID.fromString(payload.get("leadId").toString());
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, leadId).orElseThrow();
        String title = (String) payload.getOrDefault("title", "Automated CRM Follow-up");
        String typeStr = (String) payload.getOrDefault("type", "CALL");

        LeadFollowUp fu = new LeadFollowUp();
        fu.setTenantId(tenantId);
        fu.setLeadId(lead.getId());
        fu.setTitle(title);
        fu.setDueAt(LocalDateTime.now().plusDays(1));
        fu.setStatus(FollowUpStatus.OPEN);
        fu.setAssignedTo(lead.getAssignedSalesUserId() != null ? lead.getAssignedSalesUserId() : executedBy);
        fu.setAssignedToName(lead.getAssignedSalesUserName() != null ? lead.getAssignedSalesUserName() : "Sales Representative");
        try {
            fu.setType(FollowUpType.valueOf(typeStr));
        } catch (Exception ignored) {
            fu.setType(FollowUpType.GENERAL);
        }
        fu.setNotes("Scheduled automatically by RentFlow Automation Engine (" + executedBy + ")");
        leadFollowUpRepository.save(fu);

        return ActionResult.success(
            "Created CRM follow-up task '" + title + "' for Lead " + lead.getLeadNumber(),
            Map.of("leadNumber", lead.getLeadNumber(), "followUpTitle", title, "dueAt", fu.getDueAt().toString())
        );
    }
}
