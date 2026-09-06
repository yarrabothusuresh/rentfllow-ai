package com.rentflow.automation.repository;

import com.rentflow.automation.model.BusinessSignal;
import com.rentflow.automation.model.BusinessSignalStatus;
import com.rentflow.automation.model.BusinessSignalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessSignalRepository extends JpaRepository<BusinessSignal, UUID> {
    Optional<BusinessSignal> findByTenantIdAndDedupeKey(String tenantId, String dedupeKey);
    List<BusinessSignal> findByTenantIdAndStatus(String tenantId, BusinessSignalStatus status);
    List<BusinessSignal> findByTenantIdAndSignalTypeAndStatus(String tenantId, BusinessSignalType signalType, BusinessSignalStatus status);
    List<BusinessSignal> findByTenantIdAndSourceEntityTypeAndSourceEntityId(String tenantId, String sourceEntityType, String sourceEntityId);
    long countByTenantIdAndStatus(String tenantId, BusinessSignalStatus status);
}
