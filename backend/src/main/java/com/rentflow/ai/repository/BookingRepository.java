package com.rentflow.ai.repository;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByTenantId(String tenantId);
    Page<Booking> findByTenantId(String tenantId, Pageable pageable);
    Optional<Booking> findByTenantIdAndId(String tenantId, UUID id);
    Optional<Booking> findByTenantIdAndQuoteId(String tenantId, UUID quoteId);
    List<Booking> findByTenantIdAndCustomerId(String tenantId, UUID customerId);
    Page<Booking> findByTenantIdAndCustomerId(String tenantId, UUID customerId, Pageable pageable);
    List<Booking> findByTenantIdAndEventId(String tenantId, UUID eventId);
    List<Booking> findByTenantIdAndStatus(String tenantId, BookingStatus status);
    Page<Booking> findByTenantIdAndStatus(String tenantId, BookingStatus status, Pageable pageable);
    Optional<Booking> findByBookingNumber(String bookingNumber);
    Optional<Booking> findByTenantIdAndBookingNumber(String tenantId, String bookingNumber);
    long countByTenantId(String tenantId);
    long countByTenantIdAndCustomerId(String tenantId, UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.tenantId = :tenantId AND b.id = :id")
    Optional<Booking> findByIdAndTenantIdForUpdate(@Param("tenantId") String tenantId, @Param("id") UUID id);
}
