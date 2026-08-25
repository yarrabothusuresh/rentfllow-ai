package com.rentflow.delivery.repository;

import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByTenantIdAndId(String tenantId, UUID id);
    Optional<Delivery> findByTenantIdAndDeliveryNumber(String tenantId, String deliveryNumber);
    Optional<Delivery> findByTenantIdAndWarehouseOrderId(String tenantId, UUID warehouseOrderId);

    List<Delivery> findByTenantId(String tenantId);
    List<Delivery> findByTenantIdAndScheduledDate(String tenantId, LocalDate scheduledDate);
    List<Delivery> findByTenantIdAndDriverId(String tenantId, UUID driverId);
    List<Delivery> findByTenantIdAndVehicleId(String tenantId, UUID vehicleId);
    List<Delivery> findByTenantIdAndStatus(String tenantId, DeliveryStatus status);
    List<Delivery> findByTenantIdAndCustomerId(String tenantId, UUID customerId);

    List<Delivery> findByTenantIdAndDriverIdAndScheduledDateAndStatusNotIn(String tenantId, UUID driverId, LocalDate scheduledDate, List<DeliveryStatus> statuses);
    List<Delivery> findByTenantIdAndVehicleIdAndScheduledDateAndStatusNotIn(String tenantId, UUID vehicleId, LocalDate scheduledDate, List<DeliveryStatus> statuses);

    long countByTenantId(String tenantId);
    long countByTenantIdAndStatus(String tenantId, DeliveryStatus status);
}
