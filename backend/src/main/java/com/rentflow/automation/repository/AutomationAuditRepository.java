package com.rentflow.automation.repository;

import com.rentflow.automation.model.AutomationAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AutomationAuditRepository extends JpaRepository<AutomationAudit, UUID> {
    Page<AutomationAudit> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
}
