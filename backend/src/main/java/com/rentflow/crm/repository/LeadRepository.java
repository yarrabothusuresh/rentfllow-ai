package com.rentflow.crm.repository;

import com.rentflow.crm.model.Lead;
import com.rentflow.crm.model.LeadStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository("crmLeadRepository")
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    List<Lead> findByTenantId(String tenantId);

    Page<Lead> findByTenantId(String tenantId, Pageable pageable);

    Optional<Lead> findByTenantIdAndId(String tenantId, UUID id);

    Optional<Lead> findByTenantIdAndLeadNumber(String tenantId, String leadNumber);

    Optional<Lead> findByLeadNumber(String leadNumber);

    Optional<Lead> findByTenantIdAndRentalRequestId(String tenantId, UUID rentalRequestId);

    List<Lead> findByTenantIdAndStage(String tenantId, LeadStage stage);

    List<Lead> findByTenantIdAndAssignedSalesUserId(String tenantId, String assignedSalesUserId);

    List<Lead> findByTenantIdAndCustomerId(String tenantId, UUID customerId);

    Optional<Lead> findFirstByTenantIdAndEmailIgnoreCase(String tenantId, String email);

    Optional<Lead> findFirstByTenantIdAndPhone(String tenantId, String phone);

    long countByTenantId(String tenantId);

    long countByTenantIdAndStage(String tenantId, LeadStage stage);

    long countByTenantIdAndAssignedSalesUserIdIsNull(String tenantId);

    long countByTenantIdAndAssignedSalesUserId(String tenantId, String assignedSalesUserId);

    long countByTenantIdAndAssignedSalesUserIdAndStageNotIn(String tenantId, String assignedSalesUserId, List<LeadStage> closedStages);

    @Query("SELECT l FROM CrmLead l WHERE l.tenantId = :tenantId AND " +
           "(:stage IS NULL OR l.stage = :stage) AND " +
           "(:assignedUserId IS NULL OR l.assignedSalesUserId = :assignedUserId) AND " +
           "(:unassignedOnly = false OR l.assignedSalesUserId IS NULL) AND " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(l.leadNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(l.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " (l.lastName IS NOT NULL AND LOWER(l.lastName) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           " (l.companyName IS NOT NULL AND LOWER(l.companyName) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           " LOWER(l.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " (l.phone IS NOT NULL AND LOWER(l.phone) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           " (l.eventName IS NOT NULL AND LOWER(l.eventName) LIKE LOWER(CONCAT('%', :query, '%'))))")
    Page<Lead> searchLeadsAdvanced(
            @Param("tenantId") String tenantId,
            @Param("query") String query,
            @Param("stage") LeadStage stage,
            @Param("assignedUserId") String assignedUserId,
            @Param("unassignedOnly") boolean unassignedOnly,
            Pageable pageable
    );
}
