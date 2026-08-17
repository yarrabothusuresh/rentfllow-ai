package com.rentflow.ai;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.DiscountType;
import com.rentflow.ai.model.PricingStrategy;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.CrmDataInitializer;
import com.rentflow.ai.service.QuoteDataInitializer;
import com.rentflow.ai.tool.CreateQuoteDraftTool;
import com.rentflow.ai.tool.SendQuoteActionTool;
import com.rentflow.ai.dto.ToolResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuoteEngineFoundationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CreateQuoteDraftTool createQuoteDraftTool;

    @Autowired
    private SendQuoteActionTool sendQuoteActionTool;

    private String tenantId;
    private UUID chiavariId;

    @BeforeEach
    void setUp() {
        tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;
        List<Product> products = productRepository.findByTenantId(tenantId);
        if (!products.isEmpty()) {
            chiavariId = products.get(0).getId();
        } else {
            chiavariId = UUID.randomUUID();
        }
    }

    private HttpHeaders createHeaders(String role, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Role", role != null ? role : "OWNER");
        headers.set("X-Tenant-Id", tenant != null ? tenant : tenantId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void test1_CreateQuote() {
        QuoteDTO q = new QuoteDTO();
        q.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        q.setEventId(CrmDataInitializer.EMILY_EVENT_ID);
        q.setRentalStartDateTime(LocalDateTime.of(2026, 9, 20, 8, 0));
        q.setRentalEndDateTime(LocalDateTime.of(2026, 9, 22, 18, 0));

        HttpEntity<QuoteDTO> entity = new HttpEntity<>(q, createHeaders("OWNER", tenantId));
        ResponseEntity<QuoteDTO> response = restTemplate.postForEntity("/api/quotes", entity, QuoteDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertNotNull(response.getBody().getQuoteNumber());
        assertTrue(response.getBody().getQuoteNumber().startsWith("QUO-"));
        assertEquals(QuoteStatus.DRAFT, response.getBody().getStatus());
    }

    @Test
    void test2_AddQuoteItem() {
        // Create base quote
        QuoteDTO q = new QuoteDTO();
        q.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        q.setEventId(CrmDataInitializer.EMILY_EVENT_ID);
        q.setRentalStartDateTime(LocalDateTime.of(2026, 9, 20, 8, 0));
        q.setRentalEndDateTime(LocalDateTime.of(2026, 9, 22, 18, 0));
        ResponseEntity<QuoteDTO> createResp = restTemplate.postForEntity("/api/quotes", new HttpEntity<>(q, createHeaders("OWNER", tenantId)), QuoteDTO.class);
        QuoteDTO created = createResp.getBody();
        assertNotNull(created);

        // Add line item: 250 Chiavari Chairs @ $8.00
        QuoteItemDTO item = new QuoteItemDTO();
        item.setProductId(chiavariId);
        item.setDescription("Chiavari Chair (Gold)");
        item.setQuantity(250);
        item.setUnitPrice(new BigDecimal("8.00"));
        item.setPricingStrategy(PricingStrategy.PER_EVENT);

        ResponseEntity<QuoteDTO> addResp = restTemplate.postForEntity("/api/quotes/" + created.getId() + "/items",
                new HttpEntity<>(item, createHeaders("OWNER", tenantId)), QuoteDTO.class);

        assertEquals(HttpStatus.CREATED, addResp.getStatusCode());
        assertNotNull(addResp.getBody());
        assertFalse(addResp.getBody().getItems().isEmpty());
        QuoteItemDTO addedItem = addResp.getBody().getItems().get(0);
        assertEquals(0, new BigDecimal("2000.00").compareTo(addedItem.getLineSubtotal()));
    }

    @Test
    void test3_CalculateQuote() {
        QuoteCalculationRequest req = new QuoteCalculationRequest();
        QuoteItemDTO item1 = new QuoteItemDTO();
        item1.setQuantity(250);
        item1.setUnitPrice(new BigDecimal("8.00"));
        item1.setPricingStrategy(PricingStrategy.PER_EVENT);
        req.getItems().add(item1);

        ResponseEntity<QuoteCalculationResponse> calcResp = restTemplate.postForEntity("/api/quotes/" + UUID.randomUUID() + "/calculate",
                new HttpEntity<>(req, createHeaders("OWNER", tenantId)), QuoteCalculationResponse.class);

        assertEquals(HttpStatus.OK, calcResp.getStatusCode());
        assertNotNull(calcResp.getBody());
        assertEquals(0, new BigDecimal("2000.00").compareTo(calcResp.getBody().getSubtotal()));
    }

    @Test
    void test4_PercentageDiscount() {
        QuoteCalculationRequest req = new QuoteCalculationRequest();
        QuoteItemDTO item = new QuoteItemDTO();
        item.setQuantity(100);
        item.setUnitPrice(new BigDecimal("30.00")); // Gross subtotal: $3,000.00
        req.getItems().add(item);
        req.setDiscountType(DiscountType.PERCENTAGE);
        req.setDiscountValue(new BigDecimal("10.00")); // 10%

        ResponseEntity<QuoteCalculationResponse> resp = restTemplate.postForEntity("/api/quotes/" + UUID.randomUUID() + "/calculate",
                new HttpEntity<>(req, createHeaders("OWNER", tenantId)), QuoteCalculationResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(0, new BigDecimal("300.00").compareTo(resp.getBody().getDiscountAmount()));
    }

    @Test
    void test5_FixedDiscount() {
        QuoteCalculationRequest req = new QuoteCalculationRequest();
        QuoteItemDTO item = new QuoteItemDTO();
        item.setQuantity(100);
        item.setUnitPrice(new BigDecimal("30.00")); // Gross subtotal: $3,000.00
        req.getItems().add(item);
        req.setDiscountType(DiscountType.FIXED);
        req.setDiscountValue(new BigDecimal("250.00")); // Fixed $250 discount

        ResponseEntity<QuoteCalculationResponse> resp = restTemplate.postForEntity("/api/quotes/" + UUID.randomUUID() + "/calculate",
                new HttpEntity<>(req, createHeaders("OWNER", tenantId)), QuoteCalculationResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(0, new BigDecimal("250.00").compareTo(resp.getBody().getDiscountAmount()));
    }

    @Test
    void test6_DeliveryPickupSetupFees() {
        QuoteCalculationRequest req = new QuoteCalculationRequest();
        req.setDeliveryFee(new BigDecimal("250.00"));
        req.setPickupFee(new BigDecimal("100.00"));
        req.setSetupFee(new BigDecimal("150.00"));

        ResponseEntity<QuoteCalculationResponse> resp = restTemplate.postForEntity("/api/quotes/" + UUID.randomUUID() + "/calculate",
                new HttpEntity<>(req, createHeaders("OWNER", tenantId)), QuoteCalculationResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(0, new BigDecimal("500.00").compareTo(resp.getBody().getTotalFees()));
    }

    @Test
    void test7_TaxCalculation() {
        QuoteCalculationRequest req = new QuoteCalculationRequest();
        QuoteItemDTO item = new QuoteItemDTO();
        item.setQuantity(100);
        item.setUnitPrice(new BigDecimal("10.00")); // Subtotal $1,000
        req.getItems().add(item);
        req.setTaxRate(new BigDecimal("8.25")); // 8.25%

        ResponseEntity<QuoteCalculationResponse> resp = restTemplate.postForEntity("/api/quotes/" + UUID.randomUUID() + "/calculate",
                new HttpEntity<>(req, createHeaders("OWNER", tenantId)), QuoteCalculationResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(0, new BigDecimal("82.50").compareTo(resp.getBody().getTaxAmount()));
    }

    @Test
    void test8_DepositCalculation() {
        QuoteCalculationRequest req = new QuoteCalculationRequest();
        QuoteItemDTO item = new QuoteItemDTO();
        item.setQuantity(100);
        item.setUnitPrice(new BigDecimal("40.00")); // Subtotal $4,000
        req.getItems().add(item);
        req.setTaxRate(BigDecimal.ZERO);
        req.setDepositPercentage(new BigDecimal("30.00")); // 30% deposit

        ResponseEntity<QuoteCalculationResponse> resp = restTemplate.postForEntity("/api/quotes/" + UUID.randomUUID() + "/calculate",
                new HttpEntity<>(req, createHeaders("OWNER", tenantId)), QuoteCalculationResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(0, new BigDecimal("1200.00").compareTo(resp.getBody().getDepositAmount()));
        assertEquals(0, new BigDecimal("2800.00").compareTo(resp.getBody().getRemainingBalance()));
    }

    @Test
    void test9_AvailabilityShortageWarning() {
        // Create Quote requesting 1,000 Chiavari chairs (only 500 owned)
        QuoteDTO q = new QuoteDTO();
        q.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        q.setEventId(CrmDataInitializer.EMILY_EVENT_ID);
        q.setRentalStartDateTime(LocalDateTime.of(2026, 9, 20, 8, 0));
        q.setRentalEndDateTime(LocalDateTime.of(2026, 9, 22, 18, 0));
        ResponseEntity<QuoteDTO> createResp = restTemplate.postForEntity("/api/quotes", new HttpEntity<>(q, createHeaders("OWNER", tenantId)), QuoteDTO.class);
        QuoteDTO created = createResp.getBody();
        assertNotNull(created);

        QuoteItemDTO item = new QuoteItemDTO();
        item.setProductId(chiavariId);
        item.setDescription("Chiavari Chair");
        item.setQuantity(1000); // Exceeds stock
        item.setUnitPrice(new BigDecimal("8.00"));

        ResponseEntity<QuoteDTO> addResp = restTemplate.postForEntity("/api/quotes/" + created.getId() + "/items",
                new HttpEntity<>(item, createHeaders("OWNER", tenantId)), QuoteDTO.class);

        assertEquals(HttpStatus.CREATED, addResp.getStatusCode());
        assertNotNull(addResp.getBody());
        assertTrue(addResp.getBody().isHasAvailabilityShortage());
        assertFalse(addResp.getBody().getShortageWarnings().isEmpty());
    }

    @Test
    void test10_UnavailableItemNotSilentlyFinalized() {
        QuoteDTO q = new QuoteDTO();
        q.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        q.setEventId(CrmDataInitializer.EMILY_EVENT_ID);
        q.setRentalStartDateTime(LocalDateTime.of(2026, 9, 20, 8, 0));
        q.setRentalEndDateTime(LocalDateTime.of(2026, 9, 22, 18, 0));
        ResponseEntity<QuoteDTO> createResp = restTemplate.postForEntity("/api/quotes", new HttpEntity<>(q, createHeaders("OWNER", tenantId)), QuoteDTO.class);
        QuoteDTO created = createResp.getBody();
        assertNotNull(created);

        // Add excessive quantity item
        QuoteItemDTO item = new QuoteItemDTO();
        item.setProductId(chiavariId);
        item.setDescription("Chiavari Chair");
        item.setQuantity(9999);
        item.setUnitPrice(new BigDecimal("8.00"));
        restTemplate.postForEntity("/api/quotes/" + created.getId() + "/items", new HttpEntity<>(item, createHeaders("OWNER", tenantId)), QuoteDTO.class);

        ResponseEntity<QuoteDTO> getResp = restTemplate.exchange("/api/quotes/" + created.getId(), HttpMethod.GET,
                new HttpEntity<>(createHeaders("OWNER", tenantId)), QuoteDTO.class);

        assertNotNull(getResp.getBody());
        assertTrue(getResp.getBody().isHasAvailabilityShortage());
    }

    @Test
    void test11_PriceOverrideDoesNotModifyProductStandardPrice() {
        // Get initial product price
        Product initialProd = productRepository.findById(chiavariId).orElseThrow();
        BigDecimal originalCatalogPrice = initialProd.getRentalPrice();

        // Create quote with override price $7.00
        QuoteDTO q = new QuoteDTO();
        q.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        q.setEventId(CrmDataInitializer.EMILY_EVENT_ID);
        ResponseEntity<QuoteDTO> createResp = restTemplate.postForEntity("/api/quotes", new HttpEntity<>(q, createHeaders("OWNER", tenantId)), QuoteDTO.class);
        QuoteDTO created = createResp.getBody();
        assertNotNull(created);

        QuoteItemDTO item = new QuoteItemDTO();
        item.setProductId(chiavariId);
        item.setUnitPrice(new BigDecimal("7.00")); // Override price
        item.setQuantity(10);
        restTemplate.postForEntity("/api/quotes/" + created.getId() + "/items", new HttpEntity<>(item, createHeaders("OWNER", tenantId)), QuoteDTO.class);

        // Verify product catalog rental price is unchanged
        Product recheckedProd = productRepository.findById(chiavariId).orElseThrow();
        assertEquals(originalCatalogPrice, recheckedProd.getRentalPrice());
    }

    @Test
    void test12_SalesDiscountPermissionLimit() {
        QuoteDTO q = new QuoteDTO();
        q.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        q.setEventId(CrmDataInitializer.EMILY_EVENT_ID);
        q.setSubtotal(new BigDecimal("1000.00"));
        q.setDiscountAmount(new BigDecimal("300.00")); // 30% discount exceeds SALES limit of 20%

        HttpEntity<QuoteDTO> entity = new HttpEntity<>(q, createHeaders("SALES", tenantId));
        ResponseEntity<String> response = restTemplate.postForEntity("/api/quotes", entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Discount exceeds your permission limit"));
    }

    @Test
    void test13_UnauthorizedUserCannotModifyPricing() {
        QuoteDTO q = new QuoteDTO();
        q.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        q.setEventId(CrmDataInitializer.EMILY_EVENT_ID);
        q.setSubtotal(new BigDecimal("1000.00"));
        q.setDiscountAmount(new BigDecimal("100.00")); // Unauthorized discount

        HttpEntity<QuoteDTO> entity = new HttpEntity<>(q, createHeaders("WAREHOUSE", tenantId));
        ResponseEntity<String> response = restTemplate.postForEntity("/api/quotes", entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("You do not have permission to apply discounts"));
    }

    @Test
    void test14_TenantIsolation() {
        // Create quote in tenant 1
        QuoteDTO q = new QuoteDTO();
        q.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        q.setEventId(CrmDataInitializer.EMILY_EVENT_ID);
        ResponseEntity<QuoteDTO> createResp = restTemplate.postForEntity("/api/quotes", new HttpEntity<>(q, createHeaders("OWNER", tenantId)), QuoteDTO.class);
        QuoteDTO created = createResp.getBody();
        assertNotNull(created);

        // Attempt access using Tenant B
        String otherTenant = "88888888-8888-8888-8888-888888888888";
        ResponseEntity<QuoteDTO> getResp = restTemplate.exchange("/api/quotes/" + created.getId(), HttpMethod.GET,
                new HttpEntity<>(createHeaders("OWNER", otherTenant)), QuoteDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, getResp.getStatusCode());
    }

    @Test
    void test15_DuplicateQuoteCreatesNewNumber() {
        // Find existing seed quote
        ResponseEntity<QuoteDTO[]> getResp = restTemplate.exchange("/api/quotes", HttpMethod.GET,
                new HttpEntity<>(createHeaders("OWNER", tenantId)), QuoteDTO[].class);
        assertNotNull(getResp.getBody());
        assertTrue(getResp.getBody().length > 0);
        QuoteDTO original = getResp.getBody()[0];

        // Duplicate quote
        ResponseEntity<QuoteDTO> dupResp = restTemplate.postForEntity("/api/quotes/" + original.getId() + "/duplicate",
                new HttpEntity<>(new QuoteDuplicateRequest(), createHeaders("OWNER", tenantId)), QuoteDTO.class);

        assertEquals(HttpStatus.CREATED, dupResp.getStatusCode());
        assertNotNull(dupResp.getBody());
        assertNotEquals(original.getId(), dupResp.getBody().getId());
        assertNotEquals(original.getQuoteNumber(), dupResp.getBody().getQuoteNumber());
        assertEquals(QuoteStatus.DRAFT, dupResp.getBody().getStatus());
    }

    @Test
    void test16_ExpiredQuoteValidation() {
        QuoteDTO q = new QuoteDTO();
        q.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        q.setEventId(CrmDataInitializer.EMILY_EVENT_ID);
        q.setStatus(QuoteStatus.EXPIRED);
        ResponseEntity<QuoteDTO> createResp = restTemplate.postForEntity("/api/quotes", new HttpEntity<>(q, createHeaders("OWNER", tenantId)), QuoteDTO.class);
        QuoteDTO created = createResp.getBody();
        assertNotNull(created);

        // Attempt to update expired quote
        created.setNotes("Updating expired quote");
        ResponseEntity<String> updateResp = restTemplate.exchange("/api/quotes/" + created.getId(), HttpMethod.PUT,
                new HttpEntity<>(created, createHeaders("OWNER", tenantId)), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, updateResp.getStatusCode());
        assertTrue(updateResp.getBody().contains("expired or been cancelled"));
    }

    @Test
    void test17_CustomerCannotSeeInternalCost() {
        // Fetch seed quote with CUSTOMER role header
        ResponseEntity<QuoteDTO[]> getResp = restTemplate.exchange("/api/quotes", HttpMethod.GET,
                new HttpEntity<>(createHeaders("CUSTOMER", tenantId)), QuoteDTO[].class);

        assertNotNull(getResp.getBody());
        assertTrue(getResp.getBody().length > 0);
        QuoteDTO customerView = getResp.getBody()[0];

        assertNull(customerView.getInternalNotes());
        for (QuoteItemDTO item : customerView.getItems()) {
            assertNull(item.getStandardUnitPrice());
            assertNull(item.getPriceOverrideDifference());
        }
    }

    @Test
    void test18_AICreateQuoteProducesDraftOnly() {
        ToolRequest req = new ToolRequest("createQuoteDraft", Map.of(
                "customerId", CrmDataInitializer.EMILY_CUSTOMER_ID.toString(),
                "eventId", CrmDataInitializer.EMILY_EVENT_ID.toString()
        ), "OWNER", tenantId, UUID.randomUUID().toString());

        ToolResult result = createQuoteDraftTool.execute(req);

        assertEquals("OK", result.getStatus());
        assertNotNull(result.getData());
        if (result.getData() instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) result.getData();
            assertEquals(QuoteStatus.DRAFT, map.get("status"));
        }
    }

    @Test
    void test19_AICannotSendQuoteWithoutApproval() {
        // Fetch seed quote
        ResponseEntity<QuoteDTO[]> getResp = restTemplate.exchange("/api/quotes", HttpMethod.GET,
                new HttpEntity<>(createHeaders("OWNER", tenantId)), QuoteDTO[].class);
        assertNotNull(getResp.getBody());
        QuoteDTO q = getResp.getBody()[0];

        ToolRequest req = new ToolRequest("sendQuoteAction", Map.of(
                "quoteId", q.getId().toString()
        ), "OWNER", tenantId, UUID.randomUUID().toString());

        ToolResult result = sendQuoteActionTool.execute(req);

        assertEquals("ACTION_REQUIRES_APPROVAL", result.getStatus());
        assertNotNull(result.getData());
    }
}
