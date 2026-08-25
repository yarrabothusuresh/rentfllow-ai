package com.rentflow.delivery.repository;

import com.rentflow.delivery.model.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, UUID> {
    Optional<DeliveryRoute> findByTenantIdAndId(String tenantId, UUID id);
    List<DeliveryRoute> findByTenantId(String tenantId);
    List<DeliveryRoute> findByTenantIdAndRouteDate(String tenantId, LocalDate routeDate);
    long countByTenantId(String tenantId);
}
