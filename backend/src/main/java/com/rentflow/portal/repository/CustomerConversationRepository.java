package com.rentflow.portal.repository;

import com.rentflow.portal.model.CustomerConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerConversationRepository extends JpaRepository<CustomerConversation, UUID> {
    List<CustomerConversation> findByTenantIdAndCustomerIdOrderByUpdatedAtDesc(String tenantId, UUID customerId);
    Optional<CustomerConversation> findByTenantIdAndCustomerIdAndId(String tenantId, UUID customerId, UUID id);
    Optional<CustomerConversation> findByTenantIdAndBookingId(String tenantId, UUID bookingId);
    List<CustomerConversation> findByTenantIdOrderByUpdatedAtDesc(String tenantId);
}
