package com.rentflow.ai.repository;

import com.rentflow.ai.model.Quote;
import com.rentflow.ai.model.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {
    List<Quote> findByTenantId(String tenantId);
    Page<Quote> findByTenantId(String tenantId, Pageable pageable);
    Optional<Quote> findByTenantIdAndId(String tenantId, UUID id);
    Optional<Quote> findByTenantIdAndQuoteNumber(String tenantId, String quoteNumber);
    List<Quote> findByTenantIdAndCustomerId(String tenantId, UUID customerId);
    Page<Quote> findByTenantIdAndCustomerId(String tenantId, UUID customerId, Pageable pageable);
    List<Quote> findByTenantIdAndEventId(String tenantId, UUID eventId);
    List<Quote> findByTenantIdAndStatus(String tenantId, QuoteStatus status);
    Page<Quote> findByTenantIdAndStatus(String tenantId, QuoteStatus status, Pageable pageable);
    Optional<Quote> findByTenantIdAndIdempotencyKey(String tenantId, String idempotencyKey);
    Optional<Quote> findByQuoteNumber(String quoteNumber);
    long countByTenantId(String tenantId);
    long countByTenantIdAndCustomerId(String tenantId, UUID customerId);
}
