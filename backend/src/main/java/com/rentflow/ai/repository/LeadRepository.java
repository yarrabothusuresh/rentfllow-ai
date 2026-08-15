package com.rentflow.ai.repository;

import com.rentflow.ai.model.Lead;
import com.rentflow.ai.model.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {
    List<Lead> findByTenantId(String tenantId);
    Optional<Lead> findByTenantIdAndId(String tenantId, UUID id);
    List<Lead> findByTenantIdAndStatus(String tenantId, LeadStatus status);
    Optional<Lead> findFirstByTenantIdAndEmailIgnoreCase(String tenantId, String email);

    @Query("SELECT l FROM Lead l WHERE l.tenantId = :tenantId AND " +
           "(LOWER(l.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(l.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(l.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(l.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(l.companyName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Lead> searchLeads(@Param("tenantId") String tenantId, @Param("query") String query);
}
