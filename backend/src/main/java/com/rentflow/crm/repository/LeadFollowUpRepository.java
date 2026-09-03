package com.rentflow.crm.repository;

import com.rentflow.crm.model.FollowUpStatus;
import com.rentflow.crm.model.LeadFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadFollowUpRepository extends JpaRepository<LeadFollowUp, UUID> {

    List<LeadFollowUp> findByTenantIdAndLeadIdOrderByDueAtAsc(String tenantId, UUID leadId);

    Optional<LeadFollowUp> findByTenantIdAndId(String tenantId, UUID id);

    List<LeadFollowUp> findByTenantIdAndStatus(String tenantId, FollowUpStatus status);

    List<LeadFollowUp> findByTenantIdAndAssignedToAndStatus(String tenantId, String assignedTo, FollowUpStatus status);

    long countByTenantIdAndStatusAndDueAtBefore(String tenantId, FollowUpStatus status, LocalDateTime dateTime);

    long countByTenantIdAndStatusAndDueAtBetween(String tenantId, FollowUpStatus status, LocalDateTime start, LocalDateTime end);
}
