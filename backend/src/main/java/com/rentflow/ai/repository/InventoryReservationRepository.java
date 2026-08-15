package com.rentflow.ai.repository;

import com.rentflow.ai.model.InventoryReservation;
import com.rentflow.ai.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {
    List<InventoryReservation> findByTenantId(String tenantId);
    Optional<InventoryReservation> findByTenantIdAndId(String tenantId, UUID id);
    List<InventoryReservation> findByTenantIdAndProductId(String tenantId, UUID productId);
    List<InventoryReservation> findByTenantIdAndEventId(String tenantId, UUID eventId);

    @Query("SELECT r FROM InventoryReservation r WHERE r.tenantId = :tenantId AND r.productId = :productId AND " +
           "(r.status = com.rentflow.ai.model.ReservationStatus.RESERVED OR r.status = com.rentflow.ai.model.ReservationStatus.PENDING) AND " +
           "r.startDateTime < :requestedEnd AND r.endDateTime > :requestedStart")
    List<InventoryReservation> findOverlappingReservations(
            @Param("tenantId") String tenantId,
            @Param("productId") UUID productId,
            @Param("requestedStart") LocalDateTime requestedStart,
            @Param("requestedEnd") LocalDateTime requestedEnd);
}
