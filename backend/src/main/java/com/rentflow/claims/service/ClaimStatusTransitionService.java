package com.rentflow.claims.service;

import com.rentflow.claims.model.ClaimStatus;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
public class ClaimStatusTransitionService {

    public boolean isValidTransition(ClaimStatus currentStatus, ClaimStatus targetStatus) {
        if (currentStatus == targetStatus) return true;

        switch (currentStatus) {
            case OPEN:
                return EnumSet.of(ClaimStatus.UNDER_REVIEW, ClaimStatus.ESTIMATE_CREATED, ClaimStatus.WAIVED, ClaimStatus.CANCELLED).contains(targetStatus);
            case UNDER_REVIEW:
                return EnumSet.of(ClaimStatus.ESTIMATE_CREATED, ClaimStatus.CUSTOMER_REVIEW, ClaimStatus.APPROVED, ClaimStatus.WAIVED, ClaimStatus.CANCELLED).contains(targetStatus);
            case ESTIMATE_CREATED:
                return EnumSet.of(ClaimStatus.CUSTOMER_REVIEW, ClaimStatus.APPROVED, ClaimStatus.WAIVED, ClaimStatus.CANCELLED).contains(targetStatus);
            case CUSTOMER_REVIEW:
                return EnumSet.of(ClaimStatus.APPROVED, ClaimStatus.DISPUTED, ClaimStatus.WAIVED, ClaimStatus.CANCELLED).contains(targetStatus);
            case DISPUTED:
                return EnumSet.of(ClaimStatus.UNDER_REVIEW, ClaimStatus.ESTIMATE_CREATED, ClaimStatus.APPROVED, ClaimStatus.WAIVED, ClaimStatus.CANCELLED).contains(targetStatus);
            case APPROVED:
                return EnumSet.of(ClaimStatus.REPAIR_IN_PROGRESS, ClaimStatus.REPLACEMENT_REQUIRED, ClaimStatus.RESOLVED, ClaimStatus.WAIVED).contains(targetStatus);
            case REPAIR_IN_PROGRESS:
                return EnumSet.of(ClaimStatus.RESOLVED, ClaimStatus.REPLACEMENT_REQUIRED, ClaimStatus.DISPUTED).contains(targetStatus);
            case REPLACEMENT_REQUIRED:
                return EnumSet.of(ClaimStatus.RESOLVED, ClaimStatus.WAIVED).contains(targetStatus);
            case RESOLVED:
            case WAIVED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }

    public void validateTransition(ClaimStatus currentStatus, ClaimStatus targetStatus) {
        if (!isValidTransition(currentStatus, targetStatus)) {
            throw new IllegalStateException("Invalid claim status transition from " + currentStatus + " to " + targetStatus);
        }
    }
}
