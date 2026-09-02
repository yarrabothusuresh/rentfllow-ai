package com.rentflow.aisales.repository;

import com.rentflow.aisales.model.RentalInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentalInquiryRepository extends JpaRepository<RentalInquiry, UUID> {
    Optional<RentalInquiry> findByTenantIdAndConversationId(String tenantId, UUID conversationId);
    Optional<RentalInquiry> findByTenantIdAndId(String tenantId, UUID id);
    long countByTenantId(String tenantId);
}
