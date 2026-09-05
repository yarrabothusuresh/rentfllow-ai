package com.rentflow.aisales.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.ai.model.Booking;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.aisales.dto.CopilotActionProposalDTO;
import com.rentflow.aisales.model.CopilotActionProposal;
import com.rentflow.aisales.model.CopilotActionStatus;
import com.rentflow.aisales.model.CopilotActionType;
import com.rentflow.aisales.model.CopilotRiskLevel;
import com.rentflow.aisales.repository.CopilotActionProposalRepository;
import com.rentflow.crm.model.FollowUpType;
import com.rentflow.crm.model.Lead;
import com.rentflow.crm.repository.LeadRepository;
import com.rentflow.crm.service.LeadActivityService;
import com.rentflow.crm.service.LeadFollowUpService;
import com.rentflow.crm.model.ActivityType;
import com.rentflow.crm.model.ActivityDirection;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.service.DeliveryService;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationPriority;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import com.rentflow.warehouse.service.WarehouseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CopilotActionService {

    private static final Logger log = LoggerFactory.getLogger(CopilotActionService.class);

    private final CopilotActionProposalRepository proposalRepository;
    private final DeliveryService deliveryService;
    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;
    private final WarehouseService warehouseService;
    private final WarehouseOrderRepository warehouseOrderRepository;
    private final LeadFollowUpService leadFollowUpService;
    private final LeadRepository leadRepository;
    private final LeadActivityService leadActivityService;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public CopilotActionService(CopilotActionProposalRepository proposalRepository,
                                DeliveryService deliveryService,
                                DeliveryRepository deliveryRepository,
                                DriverRepository driverRepository,
                                WarehouseService warehouseService,
                                WarehouseOrderRepository warehouseOrderRepository,
                                LeadFollowUpService leadFollowUpService,
                                LeadRepository leadRepository,
                                LeadActivityService leadActivityService,
                                BookingRepository bookingRepository,
                                NotificationService notificationService,
                                ObjectMapper objectMapper) {
        this.proposalRepository = proposalRepository;
        this.deliveryService = deliveryService;
        this.deliveryRepository = deliveryRepository;
        this.driverRepository = driverRepository;
        this.warehouseService = warehouseService;
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.leadFollowUpService = leadFollowUpService;
        this.leadRepository = leadRepository;
        this.leadActivityService = leadActivityService;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CopilotActionProposalDTO proposeAction(String tenantId, UUID conversationId, String userId,
                                                CopilotActionType actionType, String targetType, String targetId,
                                                String summary, CopilotRiskLevel riskLevel,
                                                Map<String, Object> payload) {
        if (riskLevel == CopilotRiskLevel.PROHIBITED) {
            throw new IllegalArgumentException("Action type " + actionType + " is prohibited from Copilot execution.");
        }

        CopilotActionProposal proposal = new CopilotActionProposal();
        proposal.setTenantId(tenantId);
        proposal.setConversationId(conversationId);
        proposal.setUserId(userId);
        proposal.setActionType(actionType);
        proposal.setTargetType(targetType != null ? targetType : "ENTITY");
        proposal.setTargetId(targetId != null ? targetId : "N/A");
        proposal.setSummary(summary != null ? summary : "Action proposal: " + actionType);
        proposal.setRiskLevel(riskLevel != null ? riskLevel : CopilotRiskLevel.MEDIUM);
        proposal.setStatus(CopilotActionStatus.PROPOSED);
        proposal.setCreatedAt(LocalDateTime.now());
        proposal.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        try {
            proposal.setSafePayloadJson(objectMapper.writeValueAsString(payload != null ? payload : Collections.emptyMap()));
        } catch (Exception e) {
            log.warn("Failed to serialize safePayloadJson for action proposal", e);
            proposal.setSafePayloadJson("{}");
        }

        CopilotActionProposal saved = proposalRepository.save(proposal);
        return mapToDTO(saved, payload);
    }

    @Transactional
    public CopilotActionProposalDTO confirmAction(String tenantId, UUID proposalId, String userRole, String userId) {
        CopilotActionProposal proposal = proposalRepository.findByTenantIdAndId(tenantId, proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Action proposal not found: " + proposalId));

        if (proposal.getStatus() != CopilotActionStatus.PROPOSED) {
            throw new IllegalStateException("Cannot confirm proposal with status: " + proposal.getStatus());
        }

        if (proposal.getExpiresAt().isBefore(LocalDateTime.now())) {
            proposal.setStatus(CopilotActionStatus.EXPIRED);
            proposalRepository.save(proposal);
            throw new IllegalStateException("Proposal has expired. Please ask Copilot to generate a new action proposal.");
        }

        // Validate Role Permissions
        String cleanRole = userRole != null ? userRole.toUpperCase() : "CUSTOMER";
        validateRolePermission(proposal.getActionType(), cleanRole);

        // Parse Payload
        Map<String, Object> payload = parsePayload(proposal.getSafePayloadJson());

        proposal.setStatus(CopilotActionStatus.EXECUTING);
        proposal.setConfirmedAt(LocalDateTime.now());

        try {
            executeDomainMutation(tenantId, proposal, payload, cleanRole, userId);
            proposal.setStatus(CopilotActionStatus.EXECUTED);
            proposal.setExecutedAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to execute action proposal " + proposalId, e);
            proposal.setStatus(CopilotActionStatus.FAILED);
            proposal.setErrorMessage(e.getMessage());
            throw new RuntimeException("Action execution failed: " + e.getMessage(), e);
        }

        CopilotActionProposal updated = proposalRepository.save(proposal);
        return mapToDTO(updated, payload);
    }

    @Transactional
    public CopilotActionProposalDTO cancelAction(String tenantId, UUID proposalId, String userRole, String userId) {
        CopilotActionProposal proposal = proposalRepository.findByTenantIdAndId(tenantId, proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Action proposal not found: " + proposalId));

        if (proposal.getStatus() != CopilotActionStatus.PROPOSED) {
            throw new IllegalStateException("Cannot cancel proposal with status: " + proposal.getStatus());
        }

        proposal.setStatus(CopilotActionStatus.CANCELLED);
        CopilotActionProposal updated = proposalRepository.save(proposal);
        Map<String, Object> payload = parsePayload(updated.getSafePayloadJson());
        return mapToDTO(updated, payload);
    }

    public Optional<CopilotActionProposalDTO> getProposal(String tenantId, UUID proposalId) {
        return proposalRepository.findByTenantIdAndId(tenantId, proposalId)
                .map(p -> mapToDTO(p, parsePayload(p.getSafePayloadJson())));
    }

    private void validateRolePermission(CopilotActionType actionType, String role) {
        switch (actionType) {
            case ASSIGN_DRIVER:
                if (!Set.of("OWNER", "ADMIN", "OPERATIONS").contains(role)) {
                    throw new SecurityException("Role " + role + " is not authorized to assign drivers.");
                }
                break;
            case ASSIGN_WAREHOUSE_OPERATOR:
                if (!Set.of("OWNER", "ADMIN", "OPERATIONS", "WAREHOUSE").contains(role)) {
                    throw new SecurityException("Role " + role + " is not authorized to assign warehouse orders.");
                }
                break;
            case CREATE_LEAD_FOLLOW_UP:
            case ADD_INTERNAL_LEAD_NOTE:
            case ASSIGN_LEAD:
                if (!Set.of("OWNER", "ADMIN", "SALES").contains(role)) {
                    throw new SecurityException("Role " + role + " is not authorized for sales lead actions.");
                }
                break;
            case ADD_INTERNAL_BOOKING_NOTE:
                if (!Set.of("OWNER", "ADMIN", "SALES", "OPERATIONS", "FINANCE", "WAREHOUSE").contains(role)) {
                    throw new SecurityException("Role " + role + " is not authorized to add booking notes.");
                }
                break;
            case SEND_NOTIFICATION_TEMPLATE:
                if (!Set.of("OWNER", "ADMIN", "SALES", "OPERATIONS").contains(role)) {
                    throw new SecurityException("Role " + role + " is not authorized to trigger template notifications.");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown or unsupported action type: " + actionType);
        }
    }

    private void executeDomainMutation(String tenantId, CopilotActionProposal proposal,
                                       Map<String, Object> payload, String userRole, String userId) {
        String actor = userId != null ? userId : userRole;

        switch (proposal.getActionType()) {
            case ASSIGN_DRIVER: {
                String deliveryIdStr = String.valueOf(payload.getOrDefault("deliveryId", proposal.getTargetId()));
                String driverIdStr = String.valueOf(payload.get("driverId"));
                UUID deliveryId = UUID.fromString(deliveryIdStr);
                UUID driverId = UUID.fromString(driverIdStr);
                deliveryService.assignDriver(tenantId, deliveryId, driverId, actor);
                break;
            }
            case ASSIGN_WAREHOUSE_OPERATOR: {
                String orderIdStr = String.valueOf(payload.getOrDefault("orderId", proposal.getTargetId()));
                String operatorId = String.valueOf(payload.get("operatorId"));
                UUID orderId = UUID.fromString(orderIdStr);
                warehouseService.assignOrder(tenantId, orderId, operatorId, userRole);
                break;
            }
            case CREATE_LEAD_FOLLOW_UP: {
                String leadIdStr = String.valueOf(payload.getOrDefault("leadId", proposal.getTargetId()));
                UUID leadId = UUID.fromString(leadIdStr);
                String title = (String) payload.getOrDefault("title", "Copilot scheduled follow-up");
                String notes = (String) payload.getOrDefault("notes", "Created via Copilot action");
                String assignedTo = (String) payload.get("assignedTo");
                String assignedToName = (String) payload.get("assignedToName");
                leadFollowUpService.scheduleFollowUp(tenantId, leadId, FollowUpType.GENERAL, title, notes,
                        LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                        assignedTo, assignedToName, actor);
                break;
            }
            case ADD_INTERNAL_LEAD_NOTE: {
                String leadIdStr = String.valueOf(payload.getOrDefault("leadId", proposal.getTargetId()));
                UUID leadId = UUID.fromString(leadIdStr);
                String note = (String) payload.getOrDefault("note", proposal.getSummary());
                leadActivityService.logActivity(tenantId, leadId, ActivityType.NOTE, ActivityDirection.INTERNAL,
                        "Internal Note (Copilot)", note, null, null, null, null, actor);
                break;
            }
            case ADD_INTERNAL_BOOKING_NOTE: {
                String bookingIdStr = String.valueOf(payload.getOrDefault("bookingId", proposal.getTargetId()));
                UUID bookingId = UUID.fromString(bookingIdStr);
                Booking b = bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
                if (!tenantId.equals(b.getTenantId())) {
                    throw new SecurityException("Tenant isolation violation");
                }
                String note = (String) payload.getOrDefault("note", proposal.getSummary());
                String existing = b.getNotes() != null ? b.getNotes() + "\n" : "";
                b.setNotes(existing + "[" + LocalDateTime.now() + " " + actor + " via Copilot]: " + note);
                bookingRepository.save(b);
                break;
            }
            case ASSIGN_LEAD: {
                String leadIdStr = String.valueOf(payload.getOrDefault("leadId", proposal.getTargetId()));
                UUID leadId = UUID.fromString(leadIdStr);
                String salesUserId = String.valueOf(payload.get("salesUserId"));
                String salesUserName = (String) payload.get("salesUserName");
                Lead lead = leadRepository.findByTenantIdAndId(tenantId, leadId)
                        .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
                lead.setAssignedSalesUserId(salesUserId);
                if (salesUserName != null) lead.setAssignedSalesUserName(salesUserName);
                lead.setUpdatedBy(actor);
                leadRepository.save(lead);
                leadActivityService.logActivity(tenantId, leadId, ActivityType.STATUS_CHANGE, ActivityDirection.INTERNAL,
                        "Assigned to " + (salesUserName != null ? salesUserName : salesUserId),
                        "Lead assigned via Copilot confirmation", null, null, null, null, actor);
                break;
            }
            case SEND_NOTIFICATION_TEMPLATE: {
                NotificationRequestDTO notif = new NotificationRequestDTO();
                notif.setTenantId(tenantId);
                String title = (String) payload.getOrDefault("title", "RentFlow Notification");
                String message = (String) payload.getOrDefault("message", proposal.getSummary());
                notif.setCustomTitle(title);
                notif.setCustomMessage(message);
                notif.setPriority(NotificationPriority.NORMAL);
                notif.setType(NotificationType.SYSTEM);
                notificationService.sendNotification(notif);
                break;
            }
            default:
                throw new UnsupportedOperationException("Execution not implemented for " + proposal.getActionType());
        }
    }

    private Map<String, Object> parsePayload(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse safePayloadJson", e);
            return new HashMap<>();
        }
    }

    private CopilotActionProposalDTO mapToDTO(CopilotActionProposal p, Map<String, Object> payload) {
        CopilotActionProposalDTO dto = new CopilotActionProposalDTO();
        dto.setProposalId(p.getId());
        dto.setActionType(p.getActionType());
        dto.setTargetType(p.getTargetType());
        dto.setTargetId(p.getTargetId());
        dto.setSummary(p.getSummary());
        dto.setRiskLevel(p.getRiskLevel());
        dto.setStatus(p.getStatus());
        dto.setPayload(payload != null ? payload : parsePayload(p.getSafePayloadJson()));
        dto.setExpiresAt(p.getExpiresAt());
        dto.setRequiresConfirmation(p.getStatus() == CopilotActionStatus.PROPOSED);
        return dto;
    }
}
