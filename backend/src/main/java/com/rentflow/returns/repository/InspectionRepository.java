package com.rentflow.returns.repository;

import com.rentflow.returns.model.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, UUID> {

    List<Inspection> findByTenantIdAndReturnOrderId(String tenantId, UUID returnOrderId);

    Optional<Inspection> findByTenantIdAndReturnOrderItemId(String tenantId, UUID returnOrderItemId);

    List<Inspection> findByTenantId(String tenantId);
}
