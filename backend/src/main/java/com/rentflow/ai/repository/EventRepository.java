package com.rentflow.ai.repository;

import com.rentflow.ai.model.Event;
import com.rentflow.ai.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository("crmEventRepository")
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByTenantId(String tenantId);
    Optional<Event> findByTenantIdAndId(String tenantId, UUID id);
    List<Event> findByTenantIdAndCustomerId(String tenantId, UUID customerId);
    List<Event> findByTenantIdAndEventDateBetween(String tenantId, LocalDate startDate, LocalDate endDate);
    List<Event> findByTenantIdAndStatus(String tenantId, EventStatus status);
}
