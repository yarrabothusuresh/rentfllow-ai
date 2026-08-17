package com.rentflow.ai.repository;

import com.rentflow.ai.model.Quote;
import com.rentflow.ai.model.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {
    List<Quote> findByTenantId(String tenantId);
    Optional<Quote> findByTenantIdAndId(String tenantId, UUID id);
    Optional<Quote> findByTenantIdAndQuoteNumber(String tenantId, String quoteNumber);
    List<Quote> findByTenantIdAndCustomerId(String tenantId, UUID customerId);
    List<Quote> findByTenantIdAndEventId(String tenantId, UUID eventId);
    List<Quote> findByTenantIdAndStatus(String tenantId, QuoteStatus status);
    long countByTenantId(String tenantId);
}
