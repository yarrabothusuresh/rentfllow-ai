package com.rentflow.delivery.repository;

import com.rentflow.delivery.model.DeliveryAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryAuditRepository extends JpaRepository<DeliveryAudit, UUID> {
    List<DeliveryAudit> findByTenantIdAndDeliveryIdOrderByTimestampDesc(String tenantId, UUID deliveryId);
    List<DeliveryAudit> findByTenantIdAndRouteIdOrderByTimestampDesc(String tenantId, UUID routeId);
}
