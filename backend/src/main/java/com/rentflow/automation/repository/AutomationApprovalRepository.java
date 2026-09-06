package com.rentflow.automation.repository;

import com.rentflow.automation.model.AutomationApproval;
import com.rentflow.automation.model.AutomationApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutomationApprovalRepository extends JpaRepository<AutomationApproval, UUID> {
    Optional<AutomationApproval> findByIdAndTenantId(UUID id, String tenantId);
    List<AutomationApproval> findByTenantIdAndStatus(String tenantId, AutomationApprovalStatus status);
    Optional<AutomationApproval> findByTenantIdAndRecommendationIdAndStatus(String tenantId, UUID recommendationId, AutomationApprovalStatus status);
    Page<AutomationApproval> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, AutomationApprovalStatus status, Pageable pageable);
    Page<AutomationApproval> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
    long countByTenantIdAndStatus(String tenantId, AutomationApprovalStatus status);
}
