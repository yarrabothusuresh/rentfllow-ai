package com.rentflow.automation.detector;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.automation.model.BusinessSignalCategory;
import com.rentflow.automation.model.BusinessSignalSeverity;
import com.rentflow.automation.model.BusinessSignalType;
import com.rentflow.crm.model.FollowUpStatus;
import com.rentflow.crm.model.Lead;
import com.rentflow.crm.model.LeadFollowUp;
import com.rentflow.crm.model.LeadStage;
import com.rentflow.crm.repository.LeadFollowUpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class CrmSignalDetector implements BusinessSignalDetector {

    @Autowired
    private LeadFollowUpRepository leadFollowUpRepository;

    @Autowired
    @Qualifier("crmLeadRepository")
    private com.rentflow.crm.repository.LeadRepository leadRepository;

    @Override
    public List<BusinessSignalType> getSupportedTypes() {
        return List.of(
            BusinessSignalType.CRM_LEAD_OVERDUE_FOLLOW_UP,
            BusinessSignalType.CRM_HIGH_VALUE_LEAD_UNTOUCHED
        );
    }

    @Override
    public List<DetectedSignal> detect(String tenantId) {
        List<DetectedSignal> signals = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 1. CRM_LEAD_OVERDUE_FOLLOW_UP
        List<LeadFollowUp> openFollowUps = leadFollowUpRepository.findByTenantIdAndStatus(tenantId, FollowUpStatus.OPEN);
        for (LeadFollowUp fu : openFollowUps) {
            if (fu.getDueAt() != null && fu.getDueAt().isBefore(now)) {
                Lead lead = fu.getLeadId() != null ? leadRepository.findByTenantIdAndId(tenantId, fu.getLeadId()).orElse(null) : null;
                String leadNum = lead != null ? lead.getLeadNumber() : "Unknown Lead";
                String leadContact = lead != null ? (lead.getFirstName() + " " + lead.getLastName()) : "Contact";

                Map<String, Object> evidence = new LinkedHashMap<>();
                evidence.put("followUpTitle", fu.getTitle());
                evidence.put("dueAt", fu.getDueAt().toString());
                evidence.put("assignedTo", fu.getAssignedToName() != null ? fu.getAssignedToName() : fu.getAssignedTo());
                evidence.put("leadNumber", leadNum);
                evidence.put("leadContact", leadContact);

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("leadId", fu.getLeadId().toString());
                payload.put("leadNumber", leadNum);
                payload.put("title", "Overdue Catch-up: " + fu.getTitle());
                payload.put("type", fu.getType() != null ? fu.getType().name() : "CALL");

                signals.add(new DetectedSignal(
                    BusinessSignalType.CRM_LEAD_OVERDUE_FOLLOW_UP,
                    BusinessSignalCategory.CRM,
                    "LEAD_FOLLOW_UP",
                    fu.getId().toString(),
                    leadNum,
                    BusinessSignalSeverity.HIGH,
                    tenantId + ":CRM_LEAD_OVERDUE_FOLLOW_UP:" + fu.getId(),
                    evidence,
                    AutomationActionType.CREATE_LEAD_FOLLOW_UP,
                    payload,
                    "Overdue CRM Follow-up for Lead " + leadNum,
                    "Sales follow-up '" + fu.getTitle() + "' was due on " + fu.getDueAt() + " and has not been completed.",
                    "Prompt sales outreach directly correlates to lead conversion velocity."
                ));
            }
        }

        // 2. CRM_HIGH_VALUE_LEAD_UNTOUCHED
        List<Lead> leads = leadRepository.findByTenantId(tenantId);
        for (Lead lead : leads) {
            if (lead.getStage() == LeadStage.WON || lead.getStage() == LeadStage.LOST || lead.getStage() == LeadStage.DISQUALIFIED) {
                continue;
            }

            BigDecimal est = lead.getEstimatedValue();
            boolean isHighVal = est != null && est.compareTo(new BigDecimal("5000.00")) >= 0;
            boolean isUntouched = (lead.getAssignedSalesUserId() == null || lead.getStage() == LeadStage.NEW);

            if (isHighVal && isUntouched) {
                Map<String, Object> evidence = new LinkedHashMap<>();
                evidence.put("leadNumber", lead.getLeadNumber());
                evidence.put("contactName", lead.getFirstName() + " " + lead.getLastName());
                evidence.put("estimatedValue", est.toString());
                evidence.put("stage", lead.getStage().name());
                evidence.put("createdAt", lead.getCreatedAt() != null ? lead.getCreatedAt().toString() : "");

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("taskTitle", "Urgent VIP Lead Outreach: " + lead.getLeadNumber() + " ($" + est + ")");
                payload.put("entityType", "LEAD");
                payload.put("entityId", lead.getId().toString());
                payload.put("priority", "HIGH");
                payload.put("assignedRole", "ROLE_SALES");

                signals.add(new DetectedSignal(
                    BusinessSignalType.CRM_HIGH_VALUE_LEAD_UNTOUCHED,
                    BusinessSignalCategory.CRM,
                    "LEAD",
                    lead.getId().toString(),
                    lead.getLeadNumber(),
                    BusinessSignalSeverity.HIGH,
                    tenantId + ":CRM_HIGH_VALUE_LEAD_UNTOUCHED:" + lead.getId(),
                    evidence,
                    AutomationActionType.CREATE_INTERNAL_TASK,
                    payload,
                    "High-value Lead " + lead.getLeadNumber() + " ($" + est + ") requires assignment",
                    "High-value prospect " + lead.getFirstName() + " " + lead.getLastName() + " ($" + est + ") has not yet been assigned to a dedicated sales rep.",
                    "Premium inbound leads decay rapidly without prioritized same-day sales engagement."
                ));
            }
        }

        return signals;
    }
}
