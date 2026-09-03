package com.rentflow.crm.repository;

import com.rentflow.crm.model.LeadActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadActivityRepository extends JpaRepository<LeadActivity, UUID> {

    List<LeadActivity> findByTenantIdAndLeadIdOrderByOccurredAtDesc(String tenantId, UUID leadId);

    Optional<LeadActivity> findByTenantIdAndId(String tenantId, UUID id);
}
