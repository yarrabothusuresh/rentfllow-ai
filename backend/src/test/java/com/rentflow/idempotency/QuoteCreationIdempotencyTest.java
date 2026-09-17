package com.rentflow.idempotency;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.QuoteItemDTO;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Event;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.ai.service.QuoteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class QuoteCreationIdempotencyTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    @Qualifier("crmEventRepository")
    private EventRepository eventRepository;

    @Autowired
    private ProductRepository productRepository;

    private String tenantId;
    private Customer customer;
    private Event event;
    private Product product;

    @BeforeEach
    public void setUp() {
        tenantId = "tenant-quote-" + UUID.randomUUID();

        customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setCustomerNumber("CUS-" + UUID.randomUUID().toString().substring(0, 6));
        customer.setFirstName("Bruce");
        customer.setLastName("Wayne");
        customer.setEmail("bruce@wayne-enterprises.demo");
        customer = customerRepository.save(customer);

        event = new Event();
        event.setTenantId(tenantId);
        event.setEventName("Wayne Foundation Gala");
        event.setEventDate(LocalDate.now().plusDays(10));
        event.setCustomerId(customer.getId());
        event = eventRepository.save(event);

        product = new Product();
        product.setTenantId(tenantId);
        product.setName("Crystal Chandelier");
        product.setSku("CHAND-001");
        product.setQuantityOwned(10);
        product.setRentalPrice(new BigDecimal("250.00"));
        product = productRepository.save(product);
    }

    @AfterEach
    public void tearDown() {
        if (tenantId != null) {
            quoteRepository.deleteAll(quoteRepository.findByTenantId(tenantId));
            eventRepository.deleteById(event.getId());
            customerRepository.deleteById(customer.getId());
            productRepository.deleteById(product.getId());
        }
    }

    private QuoteDTO buildSampleQuoteDTO(String idempotencyKey) {
        QuoteDTO dto = new QuoteDTO();
        dto.setTenantId(tenantId);
        dto.setCustomerId(customer.getId());
        dto.setEventId(event.getId());
        dto.setIdempotencyKey(idempotencyKey);
        dto.setRentalStartDateTime(LocalDateTime.now().plusDays(5));
        dto.setRentalEndDateTime(LocalDateTime.now().plusDays(7));

        QuoteItemDTO item = new QuoteItemDTO();
        item.setProductId(product.getId());
        item.setQuantity(2);
        item.setRentalDays(2);
        item.setUnitPrice(product.getRentalPrice());
        item.setStandardUnitPrice(product.getRentalPrice());
        item.setLineTotal(product.getRentalPrice().multiply(BigDecimal.valueOf(4)));
        dto.setItems(List.of(item));

        return dto;
    }

    @Test
    @DisplayName("44. Quote Replay: Repeating quote creation with same key returns original quote & creates only 1")
    public void testQuoteCreationIdempotency_SameKeyReturnsOriginal() {
        String key = UUID.randomUUID().toString();
        QuoteDTO first = quoteService.createQuote(tenantId, buildSampleQuoteDTO(key), "SALES");
        assertNotNull(first.getId());
        assertFalse(first.isIdempotentReplay());

        for (int i = 0; i < 4; i++) {
            QuoteDTO replay = quoteService.createQuote(tenantId, buildSampleQuoteDTO(key), "SALES");
            assertEquals(first.getId(), replay.getId());
            assertEquals(first.getQuoteNumber(), replay.getQuoteNumber());
            assertTrue(replay.isIdempotentReplay());
        }

        assertEquals(1, quoteRepository.findByTenantId(tenantId).size());
    }

    @Test
    @DisplayName("44b. Distinct Quotes: Authorized new quote or revision with new key succeeds")
    public void testDistinctQuotes_CreateDistinctRecords() {
        String key1 = UUID.randomUUID().toString();
        String key2 = UUID.randomUUID().toString();

        QuoteDTO q1 = quoteService.createQuote(tenantId, buildSampleQuoteDTO(key1), "SALES");
        QuoteDTO q2 = quoteService.createQuote(tenantId, buildSampleQuoteDTO(key2), "SALES");

        assertNotEquals(q1.getId(), q2.getId());
        assertNotEquals(q1.getQuoteNumber(), q2.getQuoteNumber());
        assertEquals(2, quoteRepository.findByTenantId(tenantId).size());
    }
}
