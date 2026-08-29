package com.rentflow.calendar.repository;

import com.rentflow.calendar.model.OperationalConflict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperationalConflictRepository extends JpaRepository<OperationalConflict, UUID> {
    List<OperationalConflict> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<OperationalConflict> findByTenantIdAndStatus(String tenantId, String status);
    Optional<OperationalConflict> findByTenantIdAndId(String tenantId, UUID id);
    long countByTenantIdAndStatus(String tenantId, String status);
}
