package com.rentflow.crm.service;

import com.rentflow.crm.dto.LeadActivityDTO;
import com.rentflow.crm.model.ActivityDirection;
import com.rentflow.crm.model.ActivityType;
import com.rentflow.crm.model.CallOutcome;
import com.rentflow.crm.model.LeadActivity;
import com.rentflow.crm.repository.LeadActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeadActivityService {

    private final LeadActivityRepository activityRepository;

    public LeadActivityService(LeadActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<LeadActivityDTO> getActivities(String tenantId, UUID leadId) {
        return activityRepository.findByTenantIdAndLeadIdOrderByOccurredAtDesc(tenantId, leadId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public LeadActivityDTO logActivity(String tenantId, UUID leadId, ActivityType type, ActivityDirection direction,
                                      String subject, String summary, String notes, CallOutcome callOutcome,
                                      String referenceType, String referenceId, String user) {
        LeadActivity activity = new LeadActivity();
        activity.setTenantId(tenantId);
        activity.setLeadId(leadId);
        activity.setType(type);
        activity.setDirection(direction != null ? direction : ActivityDirection.INTERNAL);
        activity.setSubject(subject);
        activity.setSummary(summary);
        activity.setNotes(notes);
        activity.setCallOutcome(callOutcome);
        activity.setReferenceType(referenceType);
        activity.setReferenceId(referenceId);
        activity.setOccurredAt(LocalDateTime.now());
        activity.setCreatedBy(user != null ? user : "SYSTEM");

        LeadActivity saved = activityRepository.save(activity);
        return toDTO(saved);
    }

    public LeadActivityDTO logNote(String tenantId, UUID leadId, String notes, String user) {
        return logActivity(tenantId, leadId, ActivityType.NOTE, ActivityDirection.INTERNAL,
                "Internal Note Added", notes, notes, null, null, null, user);
    }

    public LeadActivityDTO logCall(String tenantId, UUID leadId, ActivityDirection direction, String summary,
                                  String notes, CallOutcome outcome, String user) {
        return logActivity(tenantId, leadId, ActivityType.CALL,
                direction != null ? direction : ActivityDirection.OUTBOUND,
                "Phone Call Logged (" + (outcome != null ? outcome : "CONNECTED") + ")",
                summary, notes, outcome, null, null, user);
    }

    public LeadActivityDTO logEmail(String tenantId, UUID leadId, ActivityDirection direction, String subject,
                                   String summary, String user) {
        return logActivity(tenantId, leadId, ActivityType.EMAIL,
                direction != null ? direction : ActivityDirection.OUTBOUND,
                subject != null ? subject : "Email Interaction",
                summary, summary, null, null, null, user);
    }

    public LeadActivityDTO toDTO(LeadActivity a) {
        LeadActivityDTO dto = new LeadActivityDTO();
        dto.setId(a.getId());
        dto.setLeadId(a.getLeadId());
        dto.setType(a.getType());
        dto.setDirection(a.getDirection());
        dto.setSubject(a.getSubject());
        dto.setSummary(a.getSummary());
        dto.setNotes(a.getNotes());
        dto.setCallOutcome(a.getCallOutcome());
        dto.setReferenceType(a.getReferenceType());
        dto.setReferenceId(a.getReferenceId());
        dto.setOccurredAt(a.getOccurredAt());
        dto.setCreatedBy(a.getCreatedBy());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
