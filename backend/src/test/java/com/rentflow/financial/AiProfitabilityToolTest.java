package com.rentflow.financial;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.model.ProductType;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.tool.CalculateProfitabilityTool;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.aisales.model.MarginStatus;
import com.rentflow.aisales.tool.CheckInternalProfitabilityTool;
import com.rentflow.analytics.service.ProfitabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AiProfitabilityToolTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private com.rentflow.ai.repository.BookingItemRepository bookingItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProfitabilityService profitabilityService;

    @Autowired
    private CalculateProfitabilityTool calculateProfitabilityTool;

    @Autowired
    private CheckInternalProfitabilityTool checkInternalProfitabilityTool;

    private String tenantId;
    private Booking testBooking;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        tenantId = "test-ai-fin-" + UUID.randomUUID();

        testProduct = new Product(
                UUID.randomUUID(),
                tenantId,
                "SKU-AI-CHAIR",
                "Deluxe Chair",
                "High end chair",
                null,
                ProductType.RENTAL_ITEM,
                ProductStatus.ACTIVE,
                new BigDecimal("20.00"),
                new BigDecimal("100.00"),
                50,
                0,
                0,
                0
        );
        productRepository.save(testProduct);

        testBooking = new Booking();
        testBooking.setTenantId(tenantId);
        testBooking.setBookingNumber("BKG-AI-" + UUID.randomUUID().toString().substring(0, 6));
        testBooking.setQuoteId(UUID.randomUUID());
        testBooking.setCustomerId(UUID.randomUUID());
        testBooking.setEventId(UUID.randomUUID());
        testBooking.setStatus(BookingStatus.CONFIRMED);
        testBooking.setBookingDate(LocalDate.now());
        testBooking.setRentalStartDateTime(LocalDateTime.now().plusDays(2));
        testBooking.setRentalEndDateTime(LocalDateTime.now().plusDays(4));
        testBooking.setSubtotal(new BigDecimal("1000.00"));
        testBooking.setDeliveryFee(new BigDecimal("100.00"));
        testBooking.setTotalAmount(new BigDecimal("1100.00"));
        testBooking.setBalanceDue(new BigDecimal("1100.00"));

        bookingRepository.save(testBooking);

        com.rentflow.ai.model.BookingItem item = new com.rentflow.ai.model.BookingItem();
        item.setBookingId(testBooking.getId());
        item.setProductId(testProduct.getId());
        item.setDescription("Deluxe Chairs (x50)");
        item.setQuantity(50);
        item.setUnitPrice(new BigDecimal("20.00"));
        item.setRentalStartDateTime(testBooking.getRentalStartDateTime());
        item.setRentalEndDateTime(testBooking.getRentalEndDateTime());
        item.setLineSubtotal(new BigDecimal("1000.00"));
        bookingItemRepository.save(item);
    }

    @Test
    @DisplayName("CalculateProfitabilityTool: Returns live authoritative numbers matching ProfitabilityService")
    void testCalculateProfitabilityToolAuthoritative() {
        ToolRequest req = new ToolRequest();
        req.setTenantId(tenantId);
        req.setParams(Map.of("bookingId", testBooking.getId().toString()));

        ToolResult result = calculateProfitabilityTool.execute(req);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof Map<?, ?>);

        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals(Boolean.TRUE, data.get("isAuthoritative"));
        assertEquals(testBooking.getBookingNumber(), data.get("bookingNumber"));

        // Direct cost in ProfitabilityService:
        // Inventory Amortization: 1000.00 * 0.28 = 280.00
        // Delivery Vehicle: 100.00 delivery fee > 0 -> 85.00 standard vehicle route
        // Total direct cost = 280.00 + 85.00 = 365.00
        // Revenue = 1100.00
        // Profit = 1100.00 - 365.00 = 735.00
        // Margin = (735.00 * 100) / 1100.00 = 66.82%
        assertEquals(new BigDecimal("1100.00"), data.get("revenue"));
        assertEquals(new BigDecimal("365.00"), data.get("estimatedCost"));
        assertEquals(new BigDecimal("735.00"), data.get("estimatedProfit"));
        assertEquals(new BigDecimal("66.82"), data.get("estimatedMargin"));
        assertEquals("HIGH_MARGIN", data.get("marginFlag"));
    }

    @Test
    @DisplayName("CalculateProfitabilityTool: Demo data mode fallback uses BigDecimal with zero float error")
    void testCalculateProfitabilityToolDemoFallback() {
        DemoDataRepository demoRepo = new DemoDataRepository();
        CalculateProfitabilityTool demoTool = new CalculateProfitabilityTool(demoRepo);

        ToolRequest req = new ToolRequest();
        req.setTenantId("demo-tenant");
        req.setParams(Map.of("bookingId", "booking-001"));

        ToolResult result = demoTool.execute(req);
        assertTrue(result.isSuccess());

        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals(new BigDecimal("6480.00"), data.get("revenue"));
        assertEquals(new BigDecimal("2920.00"), data.get("estimatedCost"));
        assertEquals(new BigDecimal("3560.00"), data.get("estimatedProfit"));
        // (3560.00 * 100) / 6480.00 = 54.938... -> 54.94%
        assertEquals(new BigDecimal("54.94"), data.get("estimatedMargin"));
        assertEquals(Boolean.TRUE, data.get("isDemoData"));
    }

    @Test
    @DisplayName("CheckInternalProfitabilityTool: Rejects CUSTOMER role with permission error")
    void testCheckInternalProfitabilityCustomerForbidden() {
        ToolCallRequestDTO req = new ToolCallRequestDTO();
        ToolCallResultDTO res = checkInternalProfitabilityTool.execute(tenantId, "CUSTOMER", req);
        assertFalse(res.isSuccess());
        assertTrue(res.getErrorMessage().contains("Permission denied"));
    }

    @Test
    @DisplayName("CheckInternalProfitabilityTool: Calculates exact margin and health status with BigDecimal")
    void testCheckInternalProfitabilityHealthyMargin() {
        ToolCallRequestDTO req = new ToolCallRequestDTO();
        req.setArguments(Map.of(
                "items", List.of(
                        Map.of("productId", testProduct.getId().toString(), "quantity", 10)
                ),
                "deliveryRequired", true
        ));

        // 10 chairs * $20 = $200 revenue + $150 delivery = $350.00 total revenue
        // Cost: 200 * 0.15 ($30 wear) + 10 * $2 ($20 handling) + $80 delivery dispatch = $130.00 total cost
        // Profit = 350.00 - 130.00 = $220.00
        // Margin = (220.00 * 100) / 350.00 = 62.86%
        ToolCallResultDTO res = checkInternalProfitabilityTool.execute(tenantId, "SALES", req);

        assertTrue(res.isSuccess());
        Map<String, Object> data = res.getData();

        assertEquals(new BigDecimal("350.00"), data.get("estimatedRevenue"));
        assertEquals(new BigDecimal("130.00"), data.get("estimatedCost"));
        assertEquals(new BigDecimal("220.00"), data.get("estimatedProfit"));
        assertEquals(new BigDecimal("62.86"), data.get("estimatedMarginPct"));
        assertEquals(MarginStatus.HEALTHY, data.get("marginStatus"));
    }
}
