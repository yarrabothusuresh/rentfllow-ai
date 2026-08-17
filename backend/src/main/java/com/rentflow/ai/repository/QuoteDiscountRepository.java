package com.rentflow.ai.repository;

import com.rentflow.ai.model.QuoteDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuoteDiscountRepository extends JpaRepository<QuoteDiscount, UUID> {
    List<QuoteDiscount> findByQuoteId(UUID quoteId);
    void deleteByQuoteId(UUID quoteId);
}
