package com.rentflow.ai.repository;

import com.rentflow.ai.model.QuoteFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuoteFeeRepository extends JpaRepository<QuoteFee, UUID> {
    List<QuoteFee> findByQuoteId(UUID quoteId);
    void deleteByQuoteId(UUID quoteId);
}
