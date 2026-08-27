package com.rentflow.claims.service;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.claims.dto.ReplacementOrderDTO;
import com.rentflow.claims.model.*;
import com.rentflow.claims.repository.ClaimAuditRepository;
import com.rentflow.claims.repository.DamageClaimRepository;
import com.rentflow.claims.repository.ReplacementOrderRepository;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReplacementService {

    private final ReplacementOrderRepository replacementRepository;
    private final DamageClaimRepository claimRepository;
    private final ProductRepository productRepository;
    private final ClaimAuditRepository auditRepository;
    private final NotificationService notificationService;

    public ReplacementService(ReplacementOrderRepository replacementRepository,
                              DamageClaimRepository claimRepository,
                              ProductRepository productRepository,
                              ClaimAuditRepository auditRepository,
                              NotificationService notificationService) {
        this.replacementRepository = replacementRepository;
        this.claimRepository = claimRepository;
        this.productRepository = productRepository;
        this.auditRepository = auditRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReplacementOrderDTO createReplacementOrder(UUID claimId, UUID productId, int quantity, String reason, BigDecimal unitCost) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        long count = replacementRepository.countByTenantId(tenantId) + 1;
        String replNum = String.format("RPL-%06d-%s", count, UUID.randomUUID().toString().substring(0, 4).toUpperCase());

        BigDecimal cost = unitCost != null ? unitCost : BigDecimal.valueOf(150.00);
        BigDecimal total = cost.multiply(BigDecimal.valueOf(quantity));

        ReplacementOrder order = new ReplacementOrder();
        order.setTenantId(tenantId);
        order.setReplacementNumber(replNum);
        order.setClaimId(claimId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setStatus(ReplacementOrderStatus.PENDING);
        order.setUnitCost(cost);
        order.setTotalCost(total);
        order.setReason(reason);

        ReplacementOrder saved = replacementRepository.save(order);

        claim.setStatus(ClaimStatus.REPLACEMENT_REQUIRED);
        claimRepository.save(claim);

        logAudit(claimId, "REPLACEMENT_CREATED", "Replacement order " + replNum + " created for " + quantity + " units.");
        if (claim.getCustomerId() != null) {
            sendNotification(tenantId, claim.getCustomerId(), NotificationType.REPLACEMENT_REQUIRED, "Replacement Required", "Replacement order " + replNum + " created.", saved.getId().toString(), "REPLACEMENT_ORDER");
        }

        return convertToDTO(saved);
    }

    @Transactional
    public ReplacementOrderDTO orderReplacement(UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ReplacementOrder order = replacementRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Replacement not found: " + id));

        order.setStatus(ReplacementOrderStatus.ORDERED);
        ReplacementOrder saved = replacementRepository.save(order);

        logAudit(order.getClaimId(), "REPLACEMENT_ORDERED", "Replacement " + order.getReplacementNumber() + " marked as ordered.");
        return convertToDTO(saved);
    }

    @Transactional
    public ReplacementOrderDTO receiveReplacement(UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ReplacementOrder order = replacementRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Replacement not found: " + id));

        order.setStatus(ReplacementOrderStatus.RECEIVED);
        ReplacementOrder saved = replacementRepository.save(order);

        logAudit(order.getClaimId(), "REPLACEMENT_RECEIVED", "Replacement " + order.getReplacementNumber() + " received.");
        return convertToDTO(saved);
    }

    @Transactional
    public ReplacementOrderDTO completeReplacement(UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ReplacementOrder order = replacementRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Replacement not found: " + id));

        order.setStatus(ReplacementOrderStatus.COMPLETED);
        ReplacementOrder saved = replacementRepository.save(order);

        // Update product owned quantity
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        if (product != null) {
            product.setQuantityOwned(product.getQuantityOwned() + order.getQuantity());
            productRepository.save(product);
        }

        logAudit(order.getClaimId(), "REPLACEMENT_COMPLETED", "Replacement " + order.getReplacementNumber() + " completed.");
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ReplacementOrderDTO> getReplacements(String search, ReplacementOrderStatus status) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<ReplacementOrder> orders = replacementRepository.findByTenantId(tenantId);

        return orders.stream()
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> {
                    if (search == null || search.trim().isEmpty()) return true;
                    String q = search.toLowerCase().trim();
                    return r.getReplacementNumber().toLowerCase().contains(q) ||
                           (r.getReason() != null && r.getReason().toLowerCase().contains(q));
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReplacementOrderDTO getReplacementById(UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ReplacementOrder order = replacementRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Replacement not found: " + id));
        return convertToDTO(order);
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

    public ReplacementOrderDTO convertToDTO(ReplacementOrder order) {
        ReplacementOrderDTO dto = new ReplacementOrderDTO();
        dto.setId(order.getId());
        dto.setReplacementNumber(order.getReplacementNumber());
        dto.setClaimId(order.getClaimId());
        dto.setProductId(order.getProductId());
        dto.setQuantity(order.getQuantity());
        dto.setStatus(order.getStatus());
        dto.setUnitCost(order.getUnitCost());
        dto.setTotalCost(order.getTotalCost());
        dto.setReason(order.getReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        Optional<DamageClaim> claim = claimRepository.findById(order.getClaimId());
        claim.ifPresent(c -> dto.setClaimNumber(c.getClaimNumber()));

        Optional<Product> product = productRepository.findById(order.getProductId());
        product.ifPresent(p -> {
            dto.setProductName(p.getName());
            dto.setProductSku(p.getSku());
        });

        return dto;
    }
}
