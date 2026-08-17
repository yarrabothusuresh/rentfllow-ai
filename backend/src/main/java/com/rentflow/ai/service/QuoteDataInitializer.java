package com.rentflow.ai.service;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.QuoteItemDTO;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.PricingStrategy;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.repository.QuoteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class QuoteDataInitializer implements CommandLineRunner {

    public static final String DEMO_QUOTE_NUMBER = "QUO-000001";

    private final QuoteRepository quoteRepository;
    private final QuoteService quoteService;
    private final ProductRepository productRepository;

    public QuoteDataInitializer(QuoteRepository quoteRepository,
                                QuoteService quoteService,
                                ProductRepository productRepository) {
        this.quoteRepository = quoteRepository;
        this.quoteService = quoteService;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (quoteRepository.count() > 0) {
            return;
        }

        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        // Find Products
        List<Product> products = productRepository.findByTenantId(tenantId);
        Optional<Product> chiavariOpt = products.stream().filter(p -> p.getSku() != null && p.getSku().contains("CHI")).findFirst();
        Optional<Product> tableOpt = products.stream().filter(p -> p.getSku() != null && p.getSku().contains("TBL")).findFirst();
        Optional<Product> uplightOpt = products.stream().filter(p -> p.getSku() != null && p.getSku().contains("LGT")).findFirst();

        QuoteDTO q = new QuoteDTO();
        q.setTenantId(tenantId);
        q.setQuoteNumber(DEMO_QUOTE_NUMBER);
        q.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        q.setEventId(CrmDataInitializer.EMILY_EVENT_ID);
        q.setStatus(QuoteStatus.DRAFT);
        q.setQuoteDate(LocalDate.of(2026, 8, 16));
        q.setValidUntil(LocalDate.of(2026, 8, 23));
        q.setRentalStartDateTime(LocalDateTime.of(2026, 9, 20, 8, 0));
        q.setRentalEndDateTime(LocalDateTime.of(2026, 9, 22, 18, 0));
        q.setDeliveryFee(new BigDecimal("250.00"));
        q.setPickupFee(new BigDecimal("100.00"));
        q.setSetupFee(new BigDecimal("150.00"));
        q.setTaxRate(new BigDecimal("8.25"));
        q.setDepositPercentage(new BigDecimal("30.00"));
        q.setNotes("Customer requested 10% package discount for Emily's Wedding reception.");
        q.setInternalNotes("High-profile wedding at Dallas Garden Hall. Ensure pristine Chiavari chairs.");

        // Item 1: 250 Chiavari Chairs @ $8.00
        QuoteItemDTO item1 = new QuoteItemDTO();
        item1.setProductId(chiavariOpt.map(Product::getId).orElse(CatalogDataInitializer.CHIAVARI_CHAIR_ID));
        item1.setDescription("Chiavari Chair (Gold)");
        item1.setQuantity(250);
        item1.setUnitPrice(new BigDecimal("8.00"));
        item1.setStandardUnitPrice(new BigDecimal("8.50"));
        item1.setPricingStrategy(PricingStrategy.PER_EVENT);
        item1.setRentalDays(2);
        q.getItems().add(item1);

        // Item 2: 25 Round Tables @ $15.00
        QuoteItemDTO item2 = new QuoteItemDTO();
        item2.setProductId(tableOpt.map(Product::getId).orElse(null));
        item2.setDescription("Round Banquet Table 60\"");
        item2.setQuantity(25);
        item2.setUnitPrice(new BigDecimal("15.00"));
        item2.setStandardUnitPrice(new BigDecimal("15.00"));
        item2.setPricingStrategy(PricingStrategy.PER_EVENT);
        item2.setRentalDays(2);
        q.getItems().add(item2);

        // Item 3: 25 White Table Linens @ $12.00
        QuoteItemDTO item3 = new QuoteItemDTO();
        item3.setDescription("White Table Linen 120\" Round");
        item3.setQuantity(25);
        item3.setUnitPrice(new BigDecimal("12.00"));
        item3.setStandardUnitPrice(new BigDecimal("12.00"));
        item3.setPricingStrategy(PricingStrategy.PER_EVENT);
        item3.setRentalDays(2);
        q.getItems().add(item3);

        // Item 4: 10 LED Uplights @ $25.00
        QuoteItemDTO item4 = new QuoteItemDTO();
        item4.setProductId(uplightOpt.map(Product::getId).orElse(null));
        item4.setDescription("Wireless LED Uplight (RGBAW)");
        item4.setQuantity(10);
        item4.setUnitPrice(new BigDecimal("25.00"));
        item4.setStandardUnitPrice(new BigDecimal("25.00"));
        item4.setPricingStrategy(PricingStrategy.PER_EVENT);
        item4.setRentalDays(2);
        q.getItems().add(item4);

        // Set Discount: $292.50 (10% of subtotal $2,925.00)
        q.setDiscountAmount(new BigDecimal("292.50"));

        try {
            quoteService.createQuote(tenantId, q, "OWNER");
            System.out.println("✅ Seeded Day 9 Demo Quote " + DEMO_QUOTE_NUMBER + " for Emily's Wedding.");
        } catch (Exception e) {
            System.err.println("⚠️ QuoteDataInitializer error: " + e.getMessage());
        }
    }
}
