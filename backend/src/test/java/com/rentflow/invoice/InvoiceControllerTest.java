package com.rentflow.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.BookingItem;
import com.rentflow.ai.repository.BookingItemRepository;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.invoice.dto.InvoiceDTO;
import com.rentflow.invoice.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String tenantId;
    private Booking booking;

    @BeforeEach
    public void setUp() {
        tenantId = "test-ctrl-tenant-" + UUID.randomUUID();

        Customer customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setCustomerNumber("CUST-CTRL-" + System.currentTimeMillis());
        customer.setFirstName("Bob");
        customer.setLastName("Builder");
        customer.setEmail("bob@builder.test");
        customer = customerRepository.save(customer);

        booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setBookingNumber("BKG-CTRL-" + System.currentTimeMillis());
        booking.setQuoteId(UUID.randomUUID());
        booking.setCustomerId(customer.getId());
        booking.setEventId(UUID.randomUUID());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDate.now());
        booking.setRentalStartDateTime(LocalDateTime.now().plusDays(2));
        booking.setRentalEndDateTime(LocalDateTime.now().plusDays(4));
        booking.setSubtotal(new BigDecimal("1500.00"));
        booking.setTotalAmount(new BigDecimal("1500.00"));
        booking.setBalanceDue(new BigDecimal("1500.00"));
        booking = bookingRepository.save(booking);

        BookingItem item = new BookingItem();
        item.setBookingId(booking.getId());
        item.setProductId(UUID.randomUUID());
        item.setDescription("Rental Stage Platform");
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("500.00"));
        item.setLineSubtotal(new BigDecimal("1500.00"));
        item.setRentalStartDateTime(booking.getRentalStartDateTime());
        item.setRentalEndDateTime(booking.getRentalEndDateTime());
        bookingItemRepository.save(item);
    }

    @Test
    @DisplayName("POST /api/invoices/from-booking/{bookingId} creates invoice")
    public void testCreateInvoiceApi() throws Exception {
        mockMvc.perform(post("/api/invoices/from-booking/" + booking.getId())
                .header("X-Tenant-Id", tenantId)
                .header("X-User-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("notes", "API Test Invoice"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceNumber").exists())
                .andExpect(jsonPath("$.subtotal").value(1500.00))
                .andExpect(jsonPath("$.totalAmount").value(1623.75))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("GET /api/invoices retrieves list")
    public void testListInvoicesApi() throws Exception {
        invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "List Test", LocalDate.now().plusDays(14), "OWNER");

        mockMvc.perform(get("/api/invoices")
                .header("X-Tenant-Id", tenantId)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].invoiceNumber").exists());
    }

    @Test
    @DisplayName("PATCH /api/invoices/{id}/status updates status")
    public void testUpdateStatusApi() throws Exception {
        InvoiceDTO created = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Status API Test", LocalDate.now().plusDays(14), "OWNER");

        mockMvc.perform(patch("/api/invoices/" + created.getId() + "/status")
                .header("X-Tenant-Id", tenantId)
                .header("X-User-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "SENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    @DisplayName("POST /api/invoices/{id}/void voids invoice")
    public void testVoidInvoiceApi() throws Exception {
        InvoiceDTO created = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Void API Test", LocalDate.now().plusDays(14), "OWNER");

        mockMvc.perform(post("/api/invoices/" + created.getId() + "/void")
                .header("X-Tenant-Id", tenantId)
                .header("X-User-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reason", "Cancelled project"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VOID"));
    }
}
