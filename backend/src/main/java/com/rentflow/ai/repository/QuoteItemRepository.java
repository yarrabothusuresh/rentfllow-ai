package com.rentflow.ai.repository;

import com.rentflow.ai.model.QuoteItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuoteItemRepository extends JpaRepository<QuoteItem, UUID> {
    List<QuoteItem> findByQuoteId(UUID quoteId);
    void deleteByQuoteId(UUID quoteId);
}
