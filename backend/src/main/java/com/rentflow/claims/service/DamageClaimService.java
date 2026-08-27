package com.rentflow.claims.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.claims.dto.*;
import com.rentflow.claims.model.*;
import com.rentflow.claims.repository.*;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.returns.model.ReturnOrder;
import com.rentflow.returns.model.ReturnOrderItem;
import com.rentflow.returns.repository.InspectionRepository;
import com.rentflow.returns.repository.ReturnOrderItemRepository;
import com.rentflow.returns.repository.ReturnOrderRepository;
import com.rentflow.security.SecurityUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DamageClaimService {

    private final DamageClaimRepository claimRepository;
    private final DamageClaimItemRepository claimItemRepository;
    private final ClaimEstimateRepository estimateRepository;
    private final ClaimAuditRepository auditRepository;
    private final ReturnOrderRepository returnOrderRepository;
    private final ReturnOrderItemRepository returnOrderItemRepository;
    private final InspectionRepository inspectionRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ClaimStatusTransitionService transitionService;
    private final ClaimBillingService billingService;
    private final NotificationService notificationService;

    public DamageClaimService(DamageClaimRepository claimRepository,
                              DamageClaimItemRepository claimItemRepository,
                              ClaimEstimateRepository estimateRepository,
                              ClaimAuditRepository auditRepository,
                              ReturnOrderRepository returnOrderRepository,
                              ReturnOrderItemRepository returnOrderItemRepository,
                              InspectionRepository inspectionRepository,
                              BookingRepository bookingRepository,
                              CustomerRepository customerRepository,
                              ProductRepository productRepository,
                              ClaimStatusTransitionService transitionService,
                              ClaimBillingService billingService,
                              NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.claimItemRepository = claimItemRepository;
        this.estimateRepository = estimateRepository;
        this.auditRepository = auditRepository;
        this.returnOrderRepository = returnOrderRepository;
        this.returnOrderItemRepository = returnOrderItemRepository;
        this.inspectionRepository = inspectionRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.transitionService = transitionService;
        this.billingService = billingService;
        this.notificationService = notificationService;
    }

    @Transactional
    public DamageClaimDTO createClaimFromReturn(UUID returnOrderId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Return order not found: " + returnOrderId));

        // Prevent duplicate claim
        Optional<DamageClaim> existing = claimRepository.findByTenantIdAndReturnOrderId(tenantId, returnOrderId);
        if (existing.isPresent()) {
            return convertToDTO(existing.get());
        }

        List<ReturnOrderItem> returnItems = returnOrderItemRepository.findByTenantIdAndReturnOrderId(tenantId, returnOrderId);
        int totalDamaged = returnItems.stream().mapToInt(ReturnOrderItem::getQuantityDamaged).sum();
        int totalMissing = returnItems.stream().mapToInt(ReturnOrderItem::getQuantityMissing).sum();

        if (totalDamaged == 0 && totalMissing == 0) {
            throw new IllegalStateException("Cannot create damage claim when there are no damaged or missing items.");
        }

        long nextNum = claimRepository.countByTenantId(tenantId) + 1;
        String claimNumber = String.format("CLM-%06d-%s", nextNum, UUID.randomUUID().toString().substring(0, 4).toUpperCase());

        DamageClaim claim = new DamageClaim();
        claim.setTenantId(tenantId);
        claim.setClaimNumber(claimNumber);
        claim.setBookingId(returnOrder.getBookingId());
        claim.setReturnOrderId(returnOrder.getId());
        claim.setCustomerId(returnOrder.getCustomerId());
        claim.setStatus(ClaimStatus.OPEN);
        claim.setPriority(returnOrder.getPriority());

        if (totalDamaged > 0 && totalMissing > 0) {
            claim.setClaimType(ClaimType.MIXED);
            claim.setDescription("Claim created for " + totalDamaged + " damaged items and " + totalMissing + " missing items.");
        } else if (totalDamaged > 0) {
            claim.setClaimType(ClaimType.DAMAGE);
            claim.setDescription("Claim created for " + totalDamaged + " damaged items.");
        } else {
            claim.setClaimType(ClaimType.MISSING);
            claim.setDescription("Claim created for " + totalMissing + " missing items.");
        }

        claim.setReportedBy(SecurityUtils.getCurrentUser());
        DamageClaim savedClaim = claimRepository.save(claim);

        BigDecimal totalEstRepair = BigDecimal.ZERO;
        BigDecimal totalEstReplace = BigDecimal.ZERO;

        for (ReturnOrderItem item : returnItems) {
            if (item.getQuantityDamaged() > 0 || item.getQuantityMissing() > 0) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                BigDecimal unitRepair = BigDecimal.valueOf(25.00); // Standard repair default
                BigDecimal unitReplace = (product != null && product.getReplacementCost() != null) ? product.getReplacementCost() : BigDecimal.valueOf(150.00);

                if (item.getQuantityDamaged() > 0) {
                    DamageClaimItem dci = new DamageClaimItem();
                    dci.setTenantId(tenantId);
                    dci.setClaimId(savedClaim.getId());
                    dci.setReturnItemId(item.getId());
                    dci.setProductId(item.getProductId());
                    dci.setProductNameSnapshot(item.getProductNameSnapshot());
                    dci.setSkuSnapshot(item.getSkuSnapshot());
                    dci.setQuantity(item.getQuantityDamaged());
                    dci.setClaimType(ClaimType.DAMAGE);
                    dci.setUnitRepairCost(unitRepair);
                    dci.setUnitReplacementCost(unitReplace);

                    BigDecimal itemEst = unitRepair.multiply(BigDecimal.valueOf(item.getQuantityDamaged()));
                    dci.setEstimatedCost(itemEst);
                    claimItemRepository.save(dci);

                    totalEstRepair = totalEstRepair.add(itemEst);
                    totalEstReplace = totalEstReplace.add(unitReplace.multiply(BigDecimal.valueOf(item.getQuantityDamaged())));
                }

                if (item.getQuantityMissing() > 0) {
                    DamageClaimItem mci = new DamageClaimItem();
                    mci.setTenantId(tenantId);
                    mci.setClaimId(savedClaim.getId());
                    mci.setReturnItemId(item.getId());
                    mci.setProductId(item.getProductId());
                    mci.setProductNameSnapshot(item.getProductNameSnapshot());
                    mci.setSkuSnapshot(item.getSkuSnapshot());
                    mci.setQuantity(item.getQuantityMissing());
                    mci.setClaimType(ClaimType.MISSING);
                    mci.setUnitRepairCost(BigDecimal.ZERO);
                    mci.setUnitReplacementCost(unitReplace);

                    BigDecimal itemEst = unitReplace.multiply(BigDecimal.valueOf(item.getQuantityMissing()));
                    mci.setEstimatedCost(itemEst);
                    claimItemRepository.save(mci);

                    totalEstReplace = totalEstReplace.add(itemEst);
                }
            }
        }

        savedClaim.setEstimatedTotalCost(totalEstRepair.add(totalEstReplace));
        claimRepository.save(savedClaim);

        logAudit(savedClaim.getId(), "DAMAGE_CLAIM_CREATED", "Claim " + claimNumber + " created from Return " + returnOrder.getReturnNumber());
        sendNotification(tenantId, savedClaim.getCustomerId(), NotificationType.DAMAGE_CLAIM_CREATED, "Damage Claim Created", "A damage claim " + claimNumber + " has been initiated for your rental return.", savedClaim.getId().toString(), "DAMAGE_CLAIM");

        return convertToDTO(savedClaim);
    }

    @Transactional(readOnly = true)
    public List<DamageClaimDTO> getClaims(String search, ClaimStatus status, ClaimType type, UUID customerId, String priority) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<DamageClaim> claims = claimRepository.findByTenantId(tenantId);

        return claims.stream()
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> type == null || c.getClaimType() == type)
                .filter(c -> customerId == null || c.getCustomerId().equals(customerId))
                .filter(c -> priority == null || priority.isEmpty() || c.getPriority().name().equalsIgnoreCase(priority))
                .filter(c -> {
                    if (search == null || search.trim().isEmpty()) return true;
                    String q = search.toLowerCase().trim();
                    return c.getClaimNumber().toLowerCase().contains(q) ||
                           (c.getDescription() != null && c.getDescription().toLowerCase().contains(q));
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DamageClaimDTO getClaimById(UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + id));
        return convertToDTO(claim);
    }

    @Transactional
    public ClaimEstimateDTO createEstimate(UUID claimId, CreateEstimateRequestDTO request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        transitionService.validateTransition(claim.getStatus(), ClaimStatus.ESTIMATE_CREATED);

        List<ClaimEstimate> existingEstimates = estimateRepository.findByTenantIdAndClaimIdOrderByVersionDesc(tenantId, claimId);
        int nextVersion = existingEstimates.isEmpty() ? 1 : existingEstimates.get(0).getVersion() + 1;

        BigDecimal repair = request.getRepairCost() != null ? request.getRepairCost() : BigDecimal.ZERO;
        BigDecimal replace = request.getReplacementCost() != null ? request.getReplacementCost() : BigDecimal.ZERO;
        BigDecimal labor = request.getLaborCost() != null ? request.getLaborCost() : BigDecimal.ZERO;
        BigDecimal transport = request.getTransportCost() != null ? request.getTransportCost() : BigDecimal.ZERO;
        BigDecimal other = request.getOtherCost() != null ? request.getOtherCost() : BigDecimal.ZERO;
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal tax = request.getTax() != null ? request.getTax() : BigDecimal.ZERO;

        BigDecimal subtotal = repair.add(replace).add(labor).add(transport).add(other).subtract(discount);
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) subtotal = BigDecimal.ZERO;
        BigDecimal total = subtotal.add(tax);

        ClaimEstimate estimate = new ClaimEstimate();
        estimate.setTenantId(tenantId);
        estimate.setClaimId(claimId);
        estimate.setVersion(nextVersion);
        estimate.setRepairCost(repair);
        estimate.setReplacementCost(replace);
        estimate.setLaborCost(labor);
        estimate.setTransportCost(transport);
        estimate.setOtherCost(other);
        estimate.setDiscount(discount);
        estimate.setTax(tax);
        estimate.setSubtotal(subtotal);
        estimate.setTotal(total);
        estimate.setCurrency(claim.getCurrency());
        estimate.setNotes(request.getNotes());
        estimate.setCreatedBy(SecurityUtils.getCurrentUser());

        ClaimEstimate savedEstimate = estimateRepository.save(estimate);

        claim.setStatus(ClaimStatus.ESTIMATE_CREATED);
        claim.setEstimatedTotalCost(total);
        claim.setAssessedBy(SecurityUtils.getCurrentUser());
        claim.setAssessedAt(LocalDateTime.now());
        claimRepository.save(claim);

        logAudit(claimId, "CLAIM_ESTIMATE_CREATED", "Created estimate version " + nextVersion + " total: $" + total);
        sendNotification(tenantId, claim.getCustomerId(), NotificationType.DAMAGE_CLAIM_ESTIMATE_CREATED, "Claim Estimate Ready", "An estimate of $" + total + " has been prepared for claim " + claim.getClaimNumber(), claimId.toString(), "DAMAGE_CLAIM");

        return convertEstimateToDTO(savedEstimate);
    }

    @Transactional
    public DamageClaimDTO sendToCustomer(UUID claimId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        transitionService.validateTransition(claim.getStatus(), ClaimStatus.CUSTOMER_REVIEW);

        claim.setStatus(ClaimStatus.CUSTOMER_REVIEW);
        claimRepository.save(claim);

        logAudit(claimId, "CLAIM_SENT_TO_CUSTOMER", "Claim " + claim.getClaimNumber() + " sent to customer for review");
        sendNotification(tenantId, claim.getCustomerId(), NotificationType.DAMAGE_CLAIM_SENT_TO_CUSTOMER, "Action Required: Damage Claim Review", "Claim " + claim.getClaimNumber() + " is ready for your review and approval.", claimId.toString(), "DAMAGE_CLAIM");

        return convertToDTO(claim);
    }

    @Transactional
    public DamageClaimDTO customerApprove(UUID claimId, String currentUser) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        if (claim.getStatus() != ClaimStatus.CUSTOMER_REVIEW && claim.getStatus() != ClaimStatus.UNDER_REVIEW && claim.getStatus() != ClaimStatus.ESTIMATE_CREATED && claim.getStatus() != ClaimStatus.DISPUTED) {
            throw new IllegalStateException("Claim must be in review or dispute state to be approved.");
        }

        transitionService.validateTransition(claim.getStatus(), ClaimStatus.APPROVED);

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedTotalCost(claim.getEstimatedTotalCost());
        claimRepository.save(claim);

        List<DamageClaimItem> items = claimItemRepository.findByTenantIdAndClaimId(tenantId, claimId);
        for (DamageClaimItem item : items) {
            item.setApprovedCost(item.getEstimatedCost());
            claimItemRepository.save(item);
        }

        // Create draft charge line (Rule 2: NO automatic payment capture)
        billingService.createDraftChargeForClaim(claim, items);

        logAudit(claimId, "CLAIM_CUSTOMER_APPROVED", "Customer approved claim " + claim.getClaimNumber());
        sendNotification(tenantId, claim.getCustomerId(), NotificationType.DAMAGE_CLAIM_APPROVED, "Claim Approved", "Thank you for approving claim " + claim.getClaimNumber(), claimId.toString(), "DAMAGE_CLAIM");

        return convertToDTO(claim);
    }

    @Transactional
    public DamageClaimDTO customerDispute(UUID claimId, String reason, String currentUser) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        transitionService.validateTransition(claim.getStatus(), ClaimStatus.DISPUTED);

        claim.setStatus(ClaimStatus.DISPUTED);
        claim.setDisputeReason(reason);
        claim.setDisputedAt(LocalDateTime.now());
        claim.setDisputedBy(currentUser != null ? currentUser : SecurityUtils.getCurrentUser());
        claimRepository.save(claim);

        logAudit(claimId, "CLAIM_CUSTOMER_DISPUTED", "Customer disputed claim: " + reason);
        sendNotification(tenantId, claim.getCustomerId(), NotificationType.DAMAGE_CLAIM_DISPUTED, "Claim Disputed", "Claim " + claim.getClaimNumber() + " has been marked as disputed and sent to management review.", claimId.toString(), "DAMAGE_CLAIM");

        return convertToDTO(claim);
    }

    @Transactional
    public DamageClaimDTO waiveClaim(UUID claimId, String waiveReason, String currentUser) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        transitionService.validateTransition(claim.getStatus(), ClaimStatus.WAIVED);

        claim.setStatus(ClaimStatus.WAIVED);
        claim.setWaiveReason(waiveReason);
        claim.setWaivedAt(LocalDateTime.now());
        claim.setWaivedBy(currentUser != null ? currentUser : SecurityUtils.getCurrentUser());
        claim.setResolution(ClaimResolution.WAIVED);
        claim.setResolvedAt(LocalDateTime.now());
        claim.setResolvedBy(currentUser != null ? currentUser : SecurityUtils.getCurrentUser());
        claimRepository.save(claim);

        logAudit(claimId, "CLAIM_WAIVED", "Claim " + claim.getClaimNumber() + " waived: " + waiveReason);
        sendNotification(tenantId, claim.getCustomerId(), NotificationType.DAMAGE_CLAIM_WAIVED, "Claim Waived", "Damage claim " + claim.getClaimNumber() + " has been waived by management.", claimId.toString(), "DAMAGE_CLAIM");

        return convertToDTO(claim);
    }

    @Transactional
    public DamageClaimDTO resolveClaim(UUID claimId, ClaimResolution resolution, String notes, String currentUser) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        DamageClaim claim = claimRepository.findByTenantIdAndId(tenantId, claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        transitionService.validateTransition(claim.getStatus(), ClaimStatus.RESOLVED);

        claim.setStatus(ClaimStatus.RESOLVED);
        claim.setResolution(resolution);
        claim.setResolutionNotes(notes);
        claim.setResolvedAt(LocalDateTime.now());
        claim.setResolvedBy(currentUser != null ? currentUser : SecurityUtils.getCurrentUser());
        claim.setFinalTotalCost(claim.getApprovedTotalCost());
        claimRepository.save(claim);

        logAudit(claimId, "CLAIM_RESOLVED", "Claim " + claim.getClaimNumber() + " resolved with outcome " + resolution);
        sendNotification(tenantId, claim.getCustomerId(), NotificationType.DAMAGE_CLAIM_RESOLVED, "Claim Resolved", "Claim " + claim.getClaimNumber() + " has been fully resolved.", claimId.toString(), "DAMAGE_CLAIM");

        return convertToDTO(claim);
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

    @Transactional(readOnly = true)
    public ClaimDashboardDTO getDashboard() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ClaimDashboardDTO dto = new ClaimDashboardDTO();

        dto.setOpenClaims(claimRepository.countByTenantIdAndStatus(tenantId, ClaimStatus.OPEN));
        dto.setUnderReview(claimRepository.countByTenantIdAndStatus(tenantId, ClaimStatus.UNDER_REVIEW));
        dto.setCustomerReview(claimRepository.countByTenantIdAndStatus(tenantId, ClaimStatus.CUSTOMER_REVIEW));
        dto.setApproved(claimRepository.countByTenantIdAndStatus(tenantId, ClaimStatus.APPROVED));
        dto.setDisputed(claimRepository.countByTenantIdAndStatus(tenantId, ClaimStatus.DISPUTED));
        dto.setRepairInProgress(claimRepository.countByTenantIdAndStatus(tenantId, ClaimStatus.REPAIR_IN_PROGRESS));
        dto.setReplacementRequired(claimRepository.countByTenantIdAndStatus(tenantId, ClaimStatus.REPLACEMENT_REQUIRED));
        dto.setResolved(claimRepository.countByTenantIdAndStatus(tenantId, ClaimStatus.RESOLVED));

        List<DamageClaim> allClaims = claimRepository.findByTenantId(tenantId);
        BigDecimal totalExposure = allClaims.stream()
                .map(c -> c.getEstimatedTotalCost() != null ? c.getEstimatedTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalApproved = allClaims.stream()
                .map(c -> c.getApprovedTotalCost() != null ? c.getApprovedTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalResolved = allClaims.stream()
                .filter(c -> c.getStatus() == ClaimStatus.RESOLVED)
                .map(c -> c.getFinalTotalCost() != null ? c.getFinalTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setEstimatedExposure(totalExposure);
        dto.setApprovedTotal(totalApproved);
        dto.setResolvedTotal(totalResolved);
        dto.setDamageRate(3.5);
        dto.setMissingRate(1.2);

        List<DamageClaimDTO> recent = allClaims.stream()
                .sorted(Comparator.comparing(DamageClaim::getCreatedAt).reversed())
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dto.setRecentClaims(recent);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<ClaimAudit> getTimeline(UUID claimId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return auditRepository.findByTenantIdAndClaimIdOrderByTimestampDesc(tenantId, claimId);
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

    public DamageClaimDTO convertToDTO(DamageClaim claim) {
        DamageClaimDTO dto = new DamageClaimDTO();
        dto.setId(claim.getId());
        dto.setClaimNumber(claim.getClaimNumber());
        dto.setBookingId(claim.getBookingId());
        dto.setReturnOrderId(claim.getReturnOrderId());
        dto.setCustomerId(claim.getCustomerId());
        dto.setStatus(claim.getStatus());
        dto.setClaimType(claim.getClaimType());
        dto.setPriority(claim.getPriority());
        dto.setDescription(claim.getDescription());
        dto.setReportedAt(claim.getReportedAt());
        dto.setReportedBy(claim.getReportedBy());
        dto.setAssessedAt(claim.getAssessedAt());
        dto.setAssessedBy(claim.getAssessedBy());
        dto.setCustomerVisible(claim.isCustomerVisible());
        dto.setCustomerNotes(claim.getCustomerNotes());
        dto.setInternalNotes(claim.getInternalNotes());
        dto.setDisputeReason(claim.getDisputeReason());
        dto.setDisputedAt(claim.getDisputedAt());
        dto.setDisputedBy(claim.getDisputedBy());
        dto.setWaiveReason(claim.getWaiveReason());
        dto.setWaivedAt(claim.getWaivedAt());
        dto.setWaivedBy(claim.getWaivedBy());
        dto.setEstimatedTotalCost(claim.getEstimatedTotalCost());
        dto.setApprovedTotalCost(claim.getApprovedTotalCost());
        dto.setFinalTotalCost(claim.getFinalTotalCost());
        dto.setCurrency(claim.getCurrency());
        dto.setResolution(claim.getResolution());
        dto.setResolutionNotes(claim.getResolutionNotes());
        dto.setResolvedAt(claim.getResolvedAt());
        dto.setResolvedBy(claim.getResolvedBy());
        dto.setCreatedAt(claim.getCreatedAt());
        dto.setUpdatedAt(claim.getUpdatedAt());

        Optional<Booking> booking = bookingRepository.findById(claim.getBookingId());
        booking.ifPresent(b -> dto.setBookingNumber(b.getBookingNumber()));

        Optional<ReturnOrder> ret = returnOrderRepository.findById(claim.getReturnOrderId());
        ret.ifPresent(r -> dto.setReturnNumber(r.getReturnNumber()));

        Optional<Customer> customer = customerRepository.findById(claim.getCustomerId());
        customer.ifPresent(c -> dto.setCustomerName((c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : "")).trim()));

        List<DamageClaimItem> items = claimItemRepository.findByTenantIdAndClaimId(claim.getTenantId(), claim.getId());
        dto.setItems(items.stream().map(this::convertItemToDTO).collect(Collectors.toList()));

        List<ClaimEstimate> estimates = estimateRepository.findByTenantIdAndClaimIdOrderByVersionDesc(claim.getTenantId(), claim.getId());
        dto.setEstimates(estimates.stream().map(this::convertEstimateToDTO).collect(Collectors.toList()));

        return dto;
    }

    private DamageClaimItemDTO convertItemToDTO(DamageClaimItem item) {
        DamageClaimItemDTO dto = new DamageClaimItemDTO();
        dto.setId(item.getId());
        dto.setClaimId(item.getClaimId());
        dto.setReturnItemId(item.getReturnItemId());
        dto.setInspectionId(item.getInspectionId());
        dto.setProductId(item.getProductId());
        dto.setProductNameSnapshot(item.getProductNameSnapshot());
        dto.setSkuSnapshot(item.getSkuSnapshot());
        dto.setQuantity(item.getQuantity());
        dto.setClaimType(item.getClaimType());
        dto.setCondition(item.getCondition());
        dto.setDamageCategory(item.getDamageCategory());
        dto.setSeverity(item.getSeverity());
        dto.setDescription(item.getDescription());
        dto.setUnitRepairCost(item.getUnitRepairCost());
        dto.setUnitReplacementCost(item.getUnitReplacementCost());
        dto.setEstimatedCost(item.getEstimatedCost());
        dto.setApprovedCost(item.getApprovedCost());
        dto.setFinalCost(item.getFinalCost());
        dto.setResolution(item.getResolution());
        dto.setNotes(item.getNotes());
        return dto;
    }

    private ClaimEstimateDTO convertEstimateToDTO(ClaimEstimate est) {
        ClaimEstimateDTO dto = new ClaimEstimateDTO();
        dto.setId(est.getId());
        dto.setClaimId(est.getClaimId());
        dto.setVersion(est.getVersion());
        dto.setRepairCost(est.getRepairCost());
        dto.setReplacementCost(est.getReplacementCost());
        dto.setLaborCost(est.getLaborCost());
        dto.setTransportCost(est.getTransportCost());
        dto.setOtherCost(est.getOtherCost());
        dto.setDiscount(est.getDiscount());
        dto.setTax(est.getTax());
        dto.setSubtotal(est.getSubtotal());
        dto.setTotal(est.getTotal());
        dto.setCurrency(est.getCurrency());
        dto.setNotes(est.getNotes());
        dto.setCreatedBy(est.getCreatedBy());
        dto.setCreatedAt(est.getCreatedAt());
        return dto;
    }
}
