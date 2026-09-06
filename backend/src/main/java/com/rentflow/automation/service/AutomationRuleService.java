package com.rentflow.automation.service;

import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.AutomationRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AutomationRuleService {

    @Autowired
    private AutomationRuleRepository ruleRepository;

    public List<AutomationRule> getRules(String tenantId) {
        ensureDefaultRules(tenantId);
        return ruleRepository.findByTenantId(tenantId);
    }

    public Optional<AutomationRule> getRuleById(UUID id) {
        return ruleRepository.findById(id);
    }

    public Optional<AutomationRule> findMatchingRule(String tenantId, BusinessSignalType signalType) {
        ensureDefaultRules(tenantId);
        List<AutomationRule> rules = ruleRepository.findByTenantIdAndSignalTypeAndEnabled(tenantId, signalType, true);
        return rules.isEmpty() ? Optional.empty() : Optional.of(rules.get(0));
    }

    @Transactional
    public AutomationRule saveRule(AutomationRule rule) {
        rule.setUpdatedAt(LocalDateTime.now());
        return ruleRepository.save(rule);
    }

    @Transactional
    public AutomationRule updateRuleMode(String tenantId, UUID ruleId, AutomationMode mode, boolean enabled) {
        AutomationRule rule = ruleRepository.findById(ruleId)
            .filter(r -> tenantId.equals(r.getTenantId()))
            .orElseThrow(() -> new IllegalArgumentException("Rule not found"));
        rule.setMode(mode);
        rule.setEnabled(enabled);
        rule.setUpdatedAt(LocalDateTime.now());
        return ruleRepository.save(rule);
    }

    @Transactional
    public synchronized void ensureDefaultRules(String tenantId) {
        if (ruleRepository.findByTenantId(tenantId).isEmpty()) {
            createRuleIfAbsent(tenantId, "RULE_EXPIRING_QUOTES", "Follow up on Expiring Quotes", 
                BusinessSignalType.QUOTE_EXPIRING_SOON, AutomationMode.APPROVAL_REQUIRED, AutomationActionType.SEND_QUOTE_REMINDER, 120, 20);

            createRuleIfAbsent(tenantId, "RULE_NEGATIVE_MARGIN", "Escalate Negative Margin Bookings", 
                BusinessSignalType.BOOKING_NEGATIVE_MARGIN, AutomationMode.APPROVAL_REQUIRED, AutomationActionType.CREATE_INTERNAL_TASK, 60, 50);

            createRuleIfAbsent(tenantId, "RULE_DELIVERY_DRIVER", "Assign Driver to Imminent Deliveries", 
                BusinessSignalType.DELIVERY_MISSING_DRIVER, AutomationMode.APPROVAL_REQUIRED, AutomationActionType.ASSIGN_DRIVER, 30, 30);

            createRuleIfAbsent(tenantId, "RULE_OVERDUE_INVOICE", "Send Overdue Invoice Reminder", 
                BusinessSignalType.INVOICE_OVERDUE, AutomationMode.APPROVAL_REQUIRED, AutomationActionType.SEND_INVOICE_REMINDER, 1440, 25);

            createRuleIfAbsent(tenantId, "RULE_UNPAID_DEPOSIT", "Request Unpaid Security Deposit", 
                BusinessSignalType.DEPOSIT_UNPAID, AutomationMode.APPROVAL_REQUIRED, AutomationActionType.SEND_DEPOSIT_REMINDER, 720, 25);

            createRuleIfAbsent(tenantId, "RULE_UNSIGNED_CONTRACT", "Remind Customer to Execute Contract", 
                BusinessSignalType.CONTRACT_UNSIGNED, AutomationMode.APPROVAL_REQUIRED, AutomationActionType.SEND_CONTRACT_REMINDER, 720, 25);

            createRuleIfAbsent(tenantId, "RULE_WAREHOUSE_SHORTAGE", "Triage Warehouse Shortages", 
                BusinessSignalType.WAREHOUSE_SHORTAGE_DETECTED, AutomationMode.APPROVAL_REQUIRED, AutomationActionType.CREATE_INTERNAL_TASK, 60, 50);

            createRuleIfAbsent(tenantId, "RULE_INTEGRATION_RETRY", "Auto-Retry Dead-letter Integration Events", 
                BusinessSignalType.INTEGRATION_DEAD_LETTER_EVENT, AutomationMode.AUTO_EXECUTE_LOW_RISK, AutomationActionType.RETRY_INTEGRATION_EVENT, 15, 100);

            createRuleIfAbsent(tenantId, "RULE_CRM_FOLLOW_UP", "Schedule Catch-up for Overdue CRM Leads", 
                BusinessSignalType.CRM_LEAD_OVERDUE_FOLLOW_UP, AutomationMode.APPROVAL_REQUIRED, AutomationActionType.CREATE_LEAD_FOLLOW_UP, 120, 30);
        }
    }

    private void createRuleIfAbsent(String tenantId, String code, String name, BusinessSignalType signalType, 
                                    AutomationMode mode, AutomationActionType actionType, int cooldownMinutes, int maxExecutionsPerDay) {
        if (ruleRepository.findByTenantIdAndRuleCode(tenantId, code).isEmpty()) {
            AutomationRule r = new AutomationRule();
            r.setTenantId(tenantId);
            r.setRuleCode(code);
            r.setName(name);
            r.setDescription("Automated governance rule for " + signalType);
            r.setSignalType(signalType);
            r.setTriggerType(AutomationTriggerType.SCHEDULED);
            r.setMode(mode);
            r.setActionType(actionType);
            r.setEnabled(true);
            r.setCooldownMinutes(cooldownMinutes);
            r.setMaxExecutionsPerDay(maxExecutionsPerDay);
            r.setAllowedRoles("ROLE_ADMIN,ROLE_MANAGER,ROLE_SALES,ROLE_LOGISTICS,ROLE_WAREHOUSE");
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
            ruleRepository.save(r);
        }
    }
}
