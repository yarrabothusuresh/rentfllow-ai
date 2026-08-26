package com.rentflow.returns.repository;

import com.rentflow.returns.model.ReturnOrder;
import com.rentflow.returns.model.ReturnOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, UUID> {

    List<ReturnOrder> findByTenantId(String tenantId);

    Optional<ReturnOrder> findByTenantIdAndId(String tenantId, UUID id);

    Optional<ReturnOrder> findByTenantIdAndBookingIdAndStatusNotIn(String tenantId, UUID bookingId, List<ReturnOrderStatus> statuses);

    List<ReturnOrder> findByTenantIdAndScheduledDate(String tenantId, LocalDate date);

    List<ReturnOrder> findByTenantIdAndDriverIdAndScheduledDateAndStatusNotIn(String tenantId, UUID driverId, LocalDate date, List<ReturnOrderStatus> statuses);

    List<ReturnOrder> findByTenantIdAndVehicleIdAndScheduledDateAndStatusNotIn(String tenantId, UUID vehicleId, LocalDate date, List<ReturnOrderStatus> statuses);

    long countByTenantId(String tenantId);
}
