package com.rentflow.claims.service;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.claims.dto.RepairDashboardDTO;
import com.rentflow.claims.dto.RepairOrderDTO;
import com.rentflow.claims.model.*;
import com.rentflow.claims.repository.ClaimAuditRepository;
import com.rentflow.claims.repository.DamageClaimRepository;
import com.rentflow.claims.repository.RepairOrderRepository;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.returns.model.InspectionCondition;
import com.rentflow.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RepairService {

    private final RepairOrderRepository repairRepository;
    private final DamageClaimRepository claimRepository;
    private final ProductRepository productRepository;
    private final ClaimAuditRepository auditRepository;
    private final NotificationService notificationService;

    public RepairService(RepairOrderRepository repairRepository,
                         DamageClaimRepository claimRepository,
                         ProductRepository productRepository,
                         ClaimAuditRepository auditRepository,
                         NotificationService notificationService) {
        this.repairRepository = repairRepository;
        this.claimRepository = claimRepository;
        this.productRepository = productRepository;
        this.auditRepository = auditRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public RepairOrderDTO createRepairOrder(UUID claimId, UUID productId, int quantity, String description, BigDecimal estimatedCost) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        long count = repairRepository.countByTenantId(tenantId) + 1;
        String repairNum = String.format("REP-%06d-%s", count, UUID.randomUUID().toString().substring(0, 4).toUpperCase());

        RepairOrder repair = new RepairOrder();
        repair.setTenantId(tenantId);
        repair.setRepairNumber(repairNum);
        repair.setClaimId(claimId);
        repair.setProductId(productId);
        repair.setQuantity(quantity);
        repair.setStatus(RepairOrderStatus.PENDING);
        repair.setDescription(description);
        repair.setEstimatedCost(estimatedCost != null ? estimatedCost : BigDecimal.ZERO);
        repair.setAssignedTo("Warehouse Technician");

        RepairOrder saved = repairRepository.save(repair);

        claim.setStatus(ClaimStatus.REPAIR_IN_PROGRESS);
        claimRepository.save(claim);

        logAudit(claimId, "REPAIR_CREATED", "Repair " + repairNum + " created for " + quantity + " items");
        return convertToDTO(saved);
    }

    @Transactional
    public RepairOrderDTO startRepair(UUID repairId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        RepairOrder repair = repairRepository.findByTenantIdAndId(tenantId, repairId)
                .orElseThrow(() -> new IllegalArgumentException("Repair order not found: " + repairId));

        repair.setStatus(RepairOrderStatus.IN_PROGRESS);
        repair.setStartedAt(LocalDateTime.now());
        RepairOrder saved = repairRepository.save(repair);

        // Product inventory state remains in MAINTENANCE during repair (Rule 17)
        logAudit(repair.getClaimId(), "REPAIR_STARTED", "Repair " + repair.getRepairNumber() + " started.");
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, repair.getClaimId()).orElse(null);
        if (claim != null && claim.getCustomerId() != null) {
            sendNotification(tenantId, claim.getCustomerId(), NotificationType.REPAIR_STARTED, "Repair Work Started", "Work has begun on repair order " + repair.getRepairNumber(), repairId.toString(), "REPAIR_ORDER");
        }

        return convertToDTO(saved);
    }

    @Transactional
    public RepairOrderDTO completeRepair(UUID repairId, int quantityRepaired, InspectionCondition conditionAfterRepair, BigDecimal actualCost, String notes) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        RepairOrder repair = repairRepository.findByTenantIdAndId(tenantId, repairId)
                .orElseThrow(() -> new IllegalArgumentException("Repair order not found: " + repairId));

        repair.setStatus(RepairOrderStatus.COMPLETED);
        repair.setCompletedAt(LocalDateTime.now());
        repair.setActualCost(actualCost != null ? actualCost : repair.getEstimatedCost());
        repair.setNotes(notes);
        RepairOrder saved = repairRepository.save(repair);

        // Restore inventory condition based on post-repair inspection
        Product product = productRepository.findByTenantIdAndId(tenantId, repair.getProductId()).orElse(null);
        if (product != null) {
            if (conditionAfterRepair == InspectionCondition.GOOD) {
                // Restoration: MAINTENANCE -> AVAILABLE
                int inMaint = Math.max(0, product.getQuantityInMaintenance() - quantityRepaired);
                product.setQuantityInMaintenance(inMaint);
                productRepository.save(product);
            } else if (conditionAfterRepair == InspectionCondition.UNUSABLE) {
                // Failed repair: MAINTENANCE -> DAMAGED
                int inMaint = Math.max(0, product.getQuantityInMaintenance() - quantityRepaired);
                product.setQuantityInMaintenance(inMaint);
                product.setQuantityDamaged(product.getQuantityDamaged() + quantityRepaired);
                productRepository.save(product);
            }
        }

        logAudit(repair.getClaimId(), "REPAIR_COMPLETED", "Repair " + repair.getRepairNumber() + " completed with condition " + conditionAfterRepair);
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, repair.getClaimId()).orElse(null);
        if (claim != null && claim.getCustomerId() != null) {
            sendNotification(tenantId, claim.getCustomerId(), NotificationType.REPAIR_COMPLETED, "Repair Order Completed", "Repair " + repair.getRepairNumber() + " completed successfully.", repairId.toString(), "REPAIR_ORDER");
        }

        return convertToDTO(saved);
    }

    @Transactional
    public RepairOrderDTO failRepair(UUID repairId, String reason) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        RepairOrder repair = repairRepository.findByTenantIdAndId(tenantId, repairId)
                .orElseThrow(() -> new IllegalArgumentException("Repair order not found: " + repairId));

        repair.setStatus(RepairOrderStatus.FAILED);
        repair.setNotes(reason);
        RepairOrder saved = repairRepository.save(repair);

        logAudit(repair.getClaimId(), "REPAIR_FAILED", "Repair " + repair.getRepairNumber() + " failed: " + reason);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<RepairOrderDTO> getRepairs(String search, RepairOrderStatus status, UUID productId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<RepairOrder> repairs = repairRepository.findByTenantId(tenantId);

        return repairs.stream()
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> productId == null || r.getProductId().equals(productId))
                .filter(r -> {
                    if (search == null || search.trim().isEmpty()) return true;
                    String q = search.toLowerCase().trim();
                    return r.getRepairNumber().toLowerCase().contains(q) ||
                           (r.getDescription() != null && r.getDescription().toLowerCase().contains(q));
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RepairOrderDTO getRepairById(UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        RepairOrder repair = repairRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Repair not found: " + id));
        return convertToDTO(repair);
    }

    @Transactional(readOnly = true)
    public RepairDashboardDTO getDashboard() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        RepairDashboardDTO dto = new RepairDashboardDTO();

        dto.setPendingRepairs(repairRepository.countByTenantIdAndStatus(tenantId, RepairOrderStatus.PENDING));
        dto.setInProgress(repairRepository.countByTenantIdAndStatus(tenantId, RepairOrderStatus.IN_PROGRESS));
        dto.setCompleted(repairRepository.countByTenantIdAndStatus(tenantId, RepairOrderStatus.COMPLETED));
        dto.setFailed(repairRepository.countByTenantIdAndStatus(tenantId, RepairOrderStatus.FAILED));

        List<RepairOrder> all = repairRepository.findByTenantId(tenantId);
        BigDecimal est = all.stream().map(r -> r.getEstimatedCost() != null ? r.getEstimatedCost() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal act = all.stream().map(r -> r.getActualCost() != null ? r.getActualCost() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setEstimatedCost(est);
        dto.setActualCost(act);

        List<RepairOrderDTO> recent = all.stream()
                .sorted(Comparator.comparing(RepairOrder::getCreatedAt).reversed())
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dto.setRecentRepairs(recent);

        return dto;
    }

    private void logAudit(UUID claimId, String action, String details) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ClaimAudit audit = new ClaimAudit();
        audit.setTenantId(tenantId);
        audit.setClaimId(claimId);
        audit.setAction(action);
        audit.setDetails(details);
        audit.setPerformedBy(SecurityUtils.getCurrentUser());
        auditRepository.save(audit);
    }

    private void sendNotification(String tenantId, UUID customerId, NotificationType type, String title, String message, String refId, String refType) {
        NotificationRequestDTO req = new NotificationRequestDTO();
        req.setTenantId(tenantId);
        req.setRecipientCustomerId(customerId);
        req.setType(type);
        req.setCustomTitle(title);
        req.setCustomMessage(message);
        req.setReferenceId(refId);
        req.setReferenceType(refType);
        notificationService.sendNotification(req);
    }

    public RepairOrderDTO convertToDTO(RepairOrder repair) {
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setId(repair.getId());
        dto.setRepairNumber(repair.getRepairNumber());
        dto.setClaimId(repair.getClaimId());
        dto.setProductId(repair.getProductId());
        dto.setQuantity(repair.getQuantity());
        dto.setStatus(repair.getStatus());
        dto.setRepairType(repair.getRepairType());
        dto.setDescription(repair.getDescription());
        dto.setEstimatedCost(repair.getEstimatedCost());
        dto.setActualCost(repair.getActualCost());
        dto.setAssignedTo(repair.getAssignedTo());
        dto.setStartedAt(repair.getStartedAt());
        dto.setCompletedAt(repair.getCompletedAt());
        dto.setNotes(repair.getNotes());
        dto.setCreatedAt(repair.getCreatedAt());
        dto.setUpdatedAt(repair.getUpdatedAt());

        Optional<DamageClaim> claim = claimRepository.findByTenantIdAndId(repair.getTenantId(), repair.getClaimId());
        claim.ifPresent(c -> dto.setClaimNumber(c.getClaimNumber()));

        Optional<Product> product = productRepository.findByTenantIdAndId(repair.getTenantId(), repair.getProductId());
        product.ifPresent(p -> {
            dto.setProductName(p.getName());
            dto.setProductSku(p.getSku());
        });

        return dto;
    }
}
