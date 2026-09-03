package com.rentflow.crm.service;

import com.rentflow.crm.dto.LeadFollowUpDTO;
import com.rentflow.crm.model.*;
import com.rentflow.crm.repository.LeadFollowUpRepository;
import com.rentflow.crm.repository.LeadRepository;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationPriority;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeadFollowUpService {

    private final LeadFollowUpRepository followUpRepository;
    private final LeadRepository leadRepository;
    private final LeadActivityService activityService;
    private final NotificationService notificationService;

    public LeadFollowUpService(LeadFollowUpRepository followUpRepository,
                               LeadRepository leadRepository,
                               LeadActivityService activityService,
                               NotificationService notificationService) {
        this.followUpRepository = followUpRepository;
        this.leadRepository = leadRepository;
        this.activityService = activityService;
        this.notificationService = notificationService;
    }

    public List<LeadFollowUpDTO> getFollowUps(String tenantId, UUID leadId) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        return followUpRepository.findByTenantIdAndLeadIdOrderByDueAtAsc(tenantId, leadId).stream()
                .map(f -> toDTO(f, lead))
                .collect(Collectors.toList());
    }

    public LeadFollowUpDTO scheduleFollowUp(String tenantId, UUID leadId, FollowUpType type,
                                           String title, String notes, LocalDateTime dueAt,
                                           String assignedTo, String assignedToName, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        LeadFollowUp followUp = new LeadFollowUp();
        followUp.setTenantId(tenantId);
        followUp.setLeadId(leadId);
        followUp.setType(type != null ? type : FollowUpType.GENERAL);
        followUp.setTitle(title != null && !title.trim().isEmpty() ? title.trim() : "Follow-up task");
        followUp.setNotes(notes);
        followUp.setDueAt(dueAt != null ? dueAt : LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        followUp.setAssignedTo(assignedTo != null ? assignedTo : lead.getAssignedSalesUserId());
        followUp.setAssignedToName(assignedToName != null ? assignedToName : lead.getAssignedSalesUserName());
        followUp.setStatus(FollowUpStatus.OPEN);
        followUp.setCreatedBy(user != null ? user : "SYSTEM");

        LeadFollowUp saved = followUpRepository.save(followUp);

        // Update lead next follow up timestamp
        updateLeadNextFollowUp(tenantId, lead);

        // Log activity
        activityService.logActivity(tenantId, leadId, ActivityType.FOLLOW_UP_CREATED, ActivityDirection.INTERNAL,
                "Follow-Up Scheduled: " + saved.getTitle(),
                "Due: " + saved.getDueAt() + " (" + saved.getType() + ")",
                notes, null, "FOLLOW_UP", saved.getId().toString(), user);

        // Notify assigned user if specified
        if (saved.getAssignedTo() != null && !saved.getAssignedTo().equalsIgnoreCase(user)) {
            try {
                UUID recipientId = null;
                try { recipientId = UUID.fromString(saved.getAssignedTo()); } catch (Exception ignored) {}
                if (recipientId != null) {
                    NotificationRequestDTO notif = new NotificationRequestDTO();
                    notif.setTenantId(tenantId);
                    notif.setRecipientUserId(recipientId);
                    notif.setCustomTitle("New Follow-Up Task: " + saved.getTitle());
                    notif.setCustomMessage("Scheduled on lead " + lead.getLeadNumber() + " due " + saved.getDueAt());
                    notif.setPriority(NotificationPriority.NORMAL);
                    notif.setType(NotificationType.SYSTEM);
                    notif.setReferenceType("CRM_LEAD");
                    notif.setReferenceId(lead.getId().toString());
                    notificationService.sendNotification(notif);
                }
            } catch (Exception ignored) {}
        }

        return toDTO(saved, lead);
    }

    public LeadFollowUpDTO completeFollowUp(String tenantId, UUID followUpId, String user) {
        LeadFollowUp followUp = followUpRepository.findByTenantIdAndId(tenantId, followUpId)
                .orElseThrow(() -> new IllegalArgumentException("Follow-up not found: " + followUpId));

        followUp.setStatus(FollowUpStatus.COMPLETED);
        followUp.setCompletedAt(LocalDateTime.now());
        followUp.setCompletedBy(user != null ? user : "SYSTEM");
        LeadFollowUp saved = followUpRepository.save(followUp);

        Lead lead = leadRepository.findByTenantIdAndId(tenantId, followUp.getLeadId()).orElse(null);
        if (lead != null) {
            updateLeadNextFollowUp(tenantId, lead);
            activityService.logActivity(tenantId, lead.getId(), ActivityType.FOLLOW_UP_COMPLETED, ActivityDirection.INTERNAL,
                    "Follow-Up Completed: " + saved.getTitle(),
                    "Completed by " + user, null, null, "FOLLOW_UP", saved.getId().toString(), user);
        }

        return toDTO(saved, lead);
    }

    public LeadFollowUpDTO cancelFollowUp(String tenantId, UUID followUpId, String user) {
        LeadFollowUp followUp = followUpRepository.findByTenantIdAndId(tenantId, followUpId)
                .orElseThrow(() -> new IllegalArgumentException("Follow-up not found: " + followUpId));

        followUp.setStatus(FollowUpStatus.CANCELLED);
        LeadFollowUp saved = followUpRepository.save(followUp);

        Lead lead = leadRepository.findByTenantIdAndId(tenantId, followUp.getLeadId()).orElse(null);
        if (lead != null) {
            updateLeadNextFollowUp(tenantId, lead);
        }

        return toDTO(saved, lead);
    }

    private void updateLeadNextFollowUp(String tenantId, Lead lead) {
        List<LeadFollowUp> openFollowUps = followUpRepository.findByTenantIdAndLeadIdOrderByDueAtAsc(tenantId, lead.getId())
                .stream()
                .filter(f -> f.getStatus() == FollowUpStatus.OPEN)
                .collect(Collectors.toList());

        if (!openFollowUps.isEmpty()) {
            lead.setNextFollowUpAt(openFollowUps.get(0).getDueAt());
        } else {
            lead.setNextFollowUpAt(null);
        }
        leadRepository.save(lead);
    }

    public LeadFollowUpDTO toDTO(LeadFollowUp f, Lead lead) {
        LeadFollowUpDTO dto = new LeadFollowUpDTO();
        dto.setId(f.getId());
        dto.setLeadId(f.getLeadId());
        dto.setLeadNumber(lead != null ? lead.getLeadNumber() : null);
        dto.setContactName(lead != null ? lead.getContactName() : null);
        dto.setAssignedTo(f.getAssignedTo());
        dto.setAssignedToName(f.getAssignedToName());
        dto.setType(f.getType());
        dto.setTitle(f.getTitle());
        dto.setNotes(f.getNotes());
        dto.setDueAt(f.getDueAt());
        dto.setStatus(f.getStatus());
        dto.setOverdue(f.isOverdue());
        dto.setCompletedAt(f.getCompletedAt());
        dto.setCompletedBy(f.getCompletedBy());
        dto.setCreatedAt(f.getCreatedAt());
        return dto;
    }
}
