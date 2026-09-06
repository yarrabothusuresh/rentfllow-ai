package com.rentflow.automation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "automation_rules", indexes = {
    @Index(name = "idx_ar_tenant_code", columnList = "tenant_id, rule_code", unique = true),
    @Index(name = "idx_ar_tenant_signal", columnList = "tenant_id, signal_type"),
    @Index(name = "idx_ar_tenant_enabled", columnList = "tenant_id, enabled")
})
public class AutomationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "rule_code", nullable = false)
    private String ruleCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false)
    private BusinessSignalType signalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private AutomationTriggerType triggerType = AutomationTriggerType.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private AutomationMode mode = AutomationMode.APPROVAL_REQUIRED;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private AutomationActionType actionType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "conditions_json", length = 2000)
    private String conditionsJson;

    @Column(name = "cooldown_minutes", nullable = false)
    private int cooldownMinutes = 60;

    @Column(name = "max_executions_per_day", nullable = false)
    private int maxExecutionsPerDay = 50;

    @Column(name = "allowed_roles")
    private String allowedRoles = "ROLE_ADMIN,ROLE_MANAGER,ROLE_SALES,ROLE_WAREHOUSE,ROLE_LOGISTICS";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public AutomationRule() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BusinessSignalType getSignalType() { return signalType; }
    public void setSignalType(BusinessSignalType signalType) { this.signalType = signalType; }

    public AutomationTriggerType getTriggerType() { return triggerType; }
    public void setTriggerType(AutomationTriggerType triggerType) { this.triggerType = triggerType; }

    public AutomationMode getMode() { return mode; }
    public void setMode(AutomationMode mode) { this.mode = mode; }

    public AutomationActionType getActionType() { return actionType; }
    public void setActionType(AutomationActionType actionType) { this.actionType = actionType; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getConditionsJson() { return conditionsJson; }
    public void setConditionsJson(String conditionsJson) { this.conditionsJson = conditionsJson; }

    public int getCooldownMinutes() { return cooldownMinutes; }
    public void setCooldownMinutes(int cooldownMinutes) { this.cooldownMinutes = cooldownMinutes; }

    public int getMaxExecutionsPerDay() { return maxExecutionsPerDay; }
    public void setMaxExecutionsPerDay(int maxExecutionsPerDay) { this.maxExecutionsPerDay = maxExecutionsPerDay; }

    public String getAllowedRoles() { return allowedRoles; }
    public void setAllowedRoles(String allowedRoles) { this.allowedRoles = allowedRoles; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
