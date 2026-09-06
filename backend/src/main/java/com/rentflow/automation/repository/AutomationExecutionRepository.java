package com.rentflow.automation.repository;

import com.rentflow.automation.model.AutomationExecution;
import com.rentflow.automation.model.AutomationExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutomationExecutionRepository extends JpaRepository<AutomationExecution, UUID> {
    Optional<AutomationExecution> findByTenantIdAndIdempotencyKey(String tenantId, String idempotencyKey);
    List<AutomationExecution> findByTenantIdAndExecutionStatus(String tenantId, AutomationExecutionStatus status);
    Page<AutomationExecution> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
    long countByTenantIdAndExecutionStatus(String tenantId, AutomationExecutionStatus status);
    long countByTenantIdAndRuleIdAndCreatedAtAfter(String tenantId, UUID ruleId, LocalDateTime after);
}
