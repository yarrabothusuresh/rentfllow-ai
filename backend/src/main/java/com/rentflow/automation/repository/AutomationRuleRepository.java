package com.rentflow.automation.repository;

import com.rentflow.automation.model.AutomationRule;
import com.rentflow.automation.model.BusinessSignalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, UUID> {
    Optional<AutomationRule> findByTenantIdAndRuleCode(String tenantId, String ruleCode);
    List<AutomationRule> findByTenantId(String tenantId);
    List<AutomationRule> findByTenantIdAndEnabled(String tenantId, boolean enabled);
    List<AutomationRule> findByTenantIdAndSignalTypeAndEnabled(String tenantId, BusinessSignalType signalType, boolean enabled);
}
