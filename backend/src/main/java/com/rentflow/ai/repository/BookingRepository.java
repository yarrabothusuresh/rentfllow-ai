package com.rentflow.ai.repository;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByTenantId(String tenantId);
    Optional<Booking> findByTenantIdAndId(String tenantId, UUID id);
    Optional<Booking> findByTenantIdAndQuoteId(String tenantId, UUID quoteId);
    List<Booking> findByTenantIdAndCustomerId(String tenantId, UUID customerId);
    List<Booking> findByTenantIdAndEventId(String tenantId, UUID eventId);
    List<Booking> findByTenantIdAndStatus(String tenantId, BookingStatus status);
    long countByTenantId(String tenantId);
}
