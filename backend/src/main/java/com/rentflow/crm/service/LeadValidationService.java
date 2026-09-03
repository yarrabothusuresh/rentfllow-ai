package com.rentflow.crm.service;

import com.rentflow.crm.dto.LeadLostRequest;
import com.rentflow.crm.dto.LeadReopenRequest;
import com.rentflow.crm.model.Lead;
import com.rentflow.crm.model.LeadStage;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class LeadValidationService {

    private static final Map<LeadStage, Set<LeadStage>> ALLOWED_TRANSITIONS = new EnumMap<>(LeadStage.class);

    static {
        ALLOWED_TRANSITIONS.put(LeadStage.NEW, EnumSet.of(LeadStage.CONTACTED, LeadStage.NEEDS_DISCOVERY, LeadStage.QUALIFIED, LeadStage.DISQUALIFIED, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.CONTACTED, EnumSet.of(LeadStage.NEEDS_DISCOVERY, LeadStage.QUALIFIED, LeadStage.DISQUALIFIED, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.NEEDS_DISCOVERY, EnumSet.of(LeadStage.QUALIFIED, LeadStage.DISQUALIFIED, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.QUALIFIED, EnumSet.of(LeadStage.QUOTE_PREPARED, LeadStage.QUOTE_SENT, LeadStage.DISQUALIFIED, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.QUOTE_PREPARED, EnumSet.of(LeadStage.QUOTE_SENT, LeadStage.FOLLOW_UP, LeadStage.NEGOTIATION, LeadStage.WON, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.QUOTE_SENT, EnumSet.of(LeadStage.FOLLOW_UP, LeadStage.NEGOTIATION, LeadStage.WON, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.FOLLOW_UP, EnumSet.of(LeadStage.NEGOTIATION, LeadStage.QUOTE_SENT, LeadStage.WON, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.NEGOTIATION, EnumSet.of(LeadStage.FOLLOW_UP, LeadStage.QUOTE_SENT, LeadStage.WON, LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.WON, EnumSet.of(LeadStage.LOST));
        ALLOWED_TRANSITIONS.put(LeadStage.LOST, EnumSet.noneOf(LeadStage.class));
        ALLOWED_TRANSITIONS.put(LeadStage.DISQUALIFIED, EnumSet.noneOf(LeadStage.class));
    }

    public void validateTransition(Lead lead, LeadStage targetStage) {
        if (lead.getStage() == targetStage) {
            return; // No-op
        }

        if (lead.getStage() == LeadStage.LOST || lead.getStage() == LeadStage.DISQUALIFIED) {
            throw new IllegalStateException("Cannot transition a closed lead (" + lead.getStage() + ") directly. Use the reopen endpoint.");
        }

        Set<LeadStage> allowed = ALLOWED_TRANSITIONS.get(lead.getStage());
        if (allowed == null || !allowed.contains(targetStage)) {
            throw new IllegalArgumentException("Invalid stage transition from " + lead.getStage() + " to " + targetStage);
        }
    }

    public void validateQualification(Lead lead) {
        if ((lead.getEmail() == null || lead.getEmail().trim().isEmpty()) &&
            (lead.getPhone() == null || lead.getPhone().trim().isEmpty())) {
            throw new IllegalArgumentException("Cannot qualify lead without at least one valid contact method (email or phone).");
        }
    }

    public void validateLost(LeadLostRequest req) {
        if (req == null || req.getLostReason() == null) {
            throw new IllegalArgumentException("A valid lostReason is required when marking a lead as LOST.");
        }
    }

    public void validateReopen(LeadReopenRequest req) {
        if (req == null || req.getReopenReason() == null || req.getReopenReason().trim().isEmpty()) {
            throw new IllegalArgumentException("A reopenReason is required to reopen a closed lead.");
        }
    }
}
