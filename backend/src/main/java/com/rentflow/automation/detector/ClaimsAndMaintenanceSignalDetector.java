package com.rentflow.automation.detector;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.automation.model.BusinessSignalCategory;
import com.rentflow.automation.model.BusinessSignalSeverity;
import com.rentflow.automation.model.BusinessSignalType;
import com.rentflow.claims.model.ClaimStatus;
import com.rentflow.claims.model.DamageClaim;
import com.rentflow.claims.repository.DamageClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class ClaimsAndMaintenanceSignalDetector implements BusinessSignalDetector {

    @Autowired
    private DamageClaimRepository damageClaimRepository;

    @Override
    public List<BusinessSignalType> getSupportedTypes() {
        return List.of(
            BusinessSignalType.DAMAGE_CLAIM_PENDING_ESTIMATE,
            BusinessSignalType.DAMAGE_CLAIM_OPEN_OVER_THRESHOLD
        );
    }

    @Override
    public List<DetectedSignal> detect(String tenantId) {
        List<DetectedSignal> signals = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        List<DamageClaim> claims = damageClaimRepository.findByTenantId(tenantId);
        for (DamageClaim claim : claims) {
            if (claim.getStatus() == ClaimStatus.RESOLVED || claim.getStatus() == ClaimStatus.WAIVED || claim.getStatus() == ClaimStatus.CANCELLED) {
                continue;
            }

            // 1. DAMAGE_CLAIM_PENDING_ESTIMATE: reported > 2 days ago and estimate is still 0
            boolean needsEstimate = (claim.getEstimatedTotalCost() == null || claim.getEstimatedTotalCost().compareTo(BigDecimal.ZERO) == 0);
            long daysReported = claim.getReportedAt() != null ? ChronoUnit.DAYS.between(claim.getReportedAt(), now) : 0;

            if (needsEstimate && daysReported >= 2) {
                Map<String, Object> evidence = new LinkedHashMap<>();
                evidence.put("claimNumber", claim.getClaimNumber());
                evidence.put("claimStatus", claim.getStatus().name());
                evidence.put("reportedAt", claim.getReportedAt() != null ? claim.getReportedAt().toString() : "");
                evidence.put("daysPending", daysReported);
                evidence.put("description", claim.getDescription());

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("claimId", claim.getId().toString());
                payload.put("claimNumber", claim.getClaimNumber());
                payload.put("priority", "HIGH");
                payload.put("action", "ESTIMATE_REQUIRED");

                signals.add(new DetectedSignal(
                    BusinessSignalType.DAMAGE_CLAIM_PENDING_ESTIMATE,
                    BusinessSignalCategory.DAMAGE,
                    "DAMAGE_CLAIM",
                    claim.getId().toString(),
                    claim.getClaimNumber(),
                    BusinessSignalSeverity.HIGH,
                    tenantId + ":DAMAGE_CLAIM_PENDING_ESTIMATE:" + claim.getId(),
                    evidence,
                    AutomationActionType.CREATE_DAMAGE_REVIEW_TASK,
                    payload,
                    "Damage Claim " + claim.getClaimNumber() + " pending repair estimate",
                    "Damage claim " + claim.getClaimNumber() + " was filed " + daysReported + " days ago without a repair or replacement cost assessment.",
                    "Timely damage estimates protect rental deposit deduction windows before final billing."
                ));
            }

            // 2. High value claim open over threshold ($1,000+)
            if (claim.getEstimatedTotalCost() != null && claim.getEstimatedTotalCost().compareTo(new BigDecimal("1000.00")) >= 0) {
                Map<String, Object> evidence = new LinkedHashMap<>();
                evidence.put("claimNumber", claim.getClaimNumber());
                evidence.put("estimatedTotalCost", claim.getEstimatedTotalCost().toString());
                evidence.put("status", claim.getStatus().name());

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("claimId", claim.getId().toString());
                payload.put("claimNumber", claim.getClaimNumber());
                payload.put("priority", "CRITICAL");
                payload.put("action", "EXPEDITE_MANAGEMENT_REVIEW");

                signals.add(new DetectedSignal(
                    BusinessSignalType.DAMAGE_CLAIM_OPEN_OVER_THRESHOLD,
                    BusinessSignalCategory.DAMAGE,
                    "DAMAGE_CLAIM",
                    claim.getId().toString(),
                    claim.getClaimNumber(),
                    BusinessSignalSeverity.HIGH,
                    tenantId + ":DAMAGE_CLAIM_OPEN_OVER_THRESHOLD:" + claim.getId(),
                    evidence,
                    AutomationActionType.CREATE_DAMAGE_REVIEW_TASK,
                    payload,
                    "High-Value Damage Claim " + claim.getClaimNumber() + " ($" + claim.getEstimatedTotalCost() + ")",
                    "Damage claim " + claim.getClaimNumber() + " involves significant replacement costs ($" + claim.getEstimatedTotalCost() + ") requiring manager review.",
                    "High-value damage claims require management oversight to determine insurance or customer recovery."
                ));
            }
        }

        return signals;
    }
}
