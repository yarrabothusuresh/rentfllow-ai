package com.rentflow.automation.action;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.claims.model.ClaimStatus;
import com.rentflow.claims.model.DamageClaim;
import com.rentflow.claims.repository.DamageClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class CreateDamageReviewTaskActionHandler implements AutomationActionHandler {

    @Autowired
    private DamageClaimRepository damageClaimRepository;

    @Override
    public AutomationActionType getActionType() {
        return AutomationActionType.CREATE_DAMAGE_REVIEW_TASK;
    }

    @Override
    public RevalidationResult revalidate(String tenantId, Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("claimId")) {
            return RevalidationResult.stale("Missing claimId in action payload");
        }
        UUID claimId;
        try {
            claimId = UUID.fromString(payload.get("claimId").toString());
        } catch (Exception e) {
            return RevalidationResult.stale("Invalid claimId: " + payload.get("claimId"));
        }

        DamageClaim claim = damageClaimRepository.findByTenantIdAndId(tenantId, claimId).orElse(null);
        if (claim == null) {
            return RevalidationResult.stale("Damage claim not found or belongs to another tenant");
        }

        if (claim.getStatus() == ClaimStatus.RESOLVED || claim.getStatus() == ClaimStatus.WAIVED || claim.getStatus() == ClaimStatus.CANCELLED) {
            return RevalidationResult.alreadyCompleted("Damage claim " + claim.getClaimNumber() + " is already " + claim.getStatus());
        }

        return RevalidationResult.valid();
    }

    @Override
    public ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy) {
        UUID claimId = UUID.fromString(payload.get("claimId").toString());
        DamageClaim claim = damageClaimRepository.findByTenantIdAndId(tenantId, claimId).orElseThrow();
        String actionType = (String) payload.getOrDefault("action", "REVIEW");

        return ActionResult.success(
            "Created damage review task for Claim " + claim.getClaimNumber() + " (" + actionType + ")",
            Map.of("claimNumber", claim.getClaimNumber(), "action", actionType, "assignedRole", "ROLE_ADMIN")
        );
    }
}
