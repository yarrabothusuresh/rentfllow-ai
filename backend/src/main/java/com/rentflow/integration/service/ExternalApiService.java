package com.rentflow.integration.service;

import com.rentflow.ai.dto.AvailabilityResultDTO;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.integration.dto.*;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExternalApiService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final AvailabilityService availabilityService;
    private final IntegrationEventService integrationEventService;

    public ExternalApiService(CustomerRepository customerRepository,
                              ProductRepository productRepository,
                              BookingRepository bookingRepository,
                              BookingItemRepository bookingItemRepository,
                              InvoiceRepository invoiceRepository,
                              QuoteRepository quoteRepository,
                              QuoteItemRepository quoteItemRepository,
                              AvailabilityService availabilityService,
                              IntegrationEventService integrationEventService) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.invoiceRepository = invoiceRepository;
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
        this.availabilityService = availabilityService;
        this.integrationEventService = integrationEventService;
    }

    public List<ExternalCustomerDTO> getCustomers(String tenantId) {
        return customerRepository.findByTenantId(tenantId).stream()
            .map(this::mapCustomer)
            .collect(Collectors.toList());
    }

    public Optional<ExternalCustomerDTO> getCustomer(String tenantId, UUID id) {
        return customerRepository.findByTenantIdAndId(tenantId, id).map(this::mapCustomer);
    }

    @Transactional
    public ExternalCustomerDTO createCustomer(String tenantId, ExternalCustomerDTO dto) {
        Customer customer = new Customer();
        customer.setTenantId(tenantId);
        long count = customerRepository.countByTenantId(tenantId) + 1;
        customer.setCustomerNumber(String.format("CUST-%06d", count));
        customer.setFirstName(dto.getName() != null ? dto.getName() : "Valued Customer");
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setCompanyName(dto.getCompany());
        customer.setBillingAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        customer.setState(dto.getState());
        customer.setZipCode(dto.getZipCode());
        customer = customerRepository.save(customer);

        integrationEventService.publishEvent(
            tenantId,
            "customer.created",
            "CUSTOMER",
            customer.getId().toString(),
            mapCustomer(customer)
        );

        return mapCustomer(customer);
    }

    public List<ExternalProductDTO> getProducts(String tenantId) {
        return productRepository.findByTenantId(tenantId).stream()
            .map(this::mapProduct)
            .collect(Collectors.toList());
    }

    public Optional<ExternalProductDTO> getProduct(String tenantId, UUID id) {
        return productRepository.findByTenantIdAndId(tenantId, id).map(this::mapProduct);
    }

    public ExternalAvailabilityDTO checkAvailability(String tenantId, UUID productId, LocalDate startDate, LocalDate endDate) {
        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atTime(23, 59, 59);
        AvailabilityResultDTO res = availabilityService.checkAvailability(tenantId, productId, 1, startDt, endDt);

        int totalQty = product.getQuantityOwned();
        int reservedQty = res.getQuantityReserved();
        int availableQty = res.getAvailableQuantity();

        return new ExternalAvailabilityDTO(
            productId,
            product.getName(),
            startDate,
            endDate,
            totalQty,
            reservedQty,
            availableQty,
            res.isAvailable()
        );
    }

    public List<ExternalBookingDTO> getBookings(String tenantId) {
        return bookingRepository.findByTenantId(tenantId).stream()
            .map(this::mapBooking)
            .collect(Collectors.toList());
    }

    public Optional<ExternalBookingDTO> getBooking(String tenantId, UUID id) {
        return bookingRepository.findByTenantIdAndId(tenantId, id).map(this::mapBooking);
    }

    public List<ExternalInvoiceDTO> getInvoices(String tenantId) {
        return invoiceRepository.findByTenantId(tenantId).stream()
            .map(this::mapInvoice)
            .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createQuoteRequest(String tenantId, ExternalQuoteRequestDTO dto) {
        // 1. Resolve or create customer
        Optional<Customer> existingCust = customerRepository.findFirstByTenantIdAndEmailIgnoreCase(tenantId, dto.getCustomerEmail());
        Customer customer = existingCust.orElseGet(() -> {
            Customer newCust = new Customer();
            newCust.setTenantId(tenantId);
            long count = customerRepository.countByTenantId(tenantId) + 1;
            newCust.setCustomerNumber(String.format("CUST-%06d", count));
            newCust.setFirstName(dto.getCustomerName() != null ? dto.getCustomerName() : "External Inquirer");
            newCust.setEmail(dto.getCustomerEmail());
            newCust.setPhone(dto.getCustomerPhone());
            newCust.setCompanyName(dto.getCompanyName());
            return customerRepository.save(newCust);
        });

        // 2. Create Quote
        Quote quote = new Quote();
        quote.setTenantId(tenantId);
        quote.setCustomerId(customer.getId());
        quote.setEventId(UUID.randomUUID());
        quote.setQuoteDate(LocalDate.now());
        quote.setValidUntil(LocalDate.now().plusDays(7));
        quote.setRentalStartDateTime(dto.getEventStartDate() != null ? dto.getEventStartDate().atTime(9, 0) : LocalDateTime.now().plusDays(7));
        quote.setRentalEndDateTime(dto.getEventEndDate() != null ? dto.getEventEndDate().atTime(18, 0) : LocalDateTime.now().plusDays(9));
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setNotes(dto.getNotes() != null ? dto.getNotes() : (dto.getEventName() != null ? dto.getEventName() : "Inbound Quote Request"));

        BigDecimal subtotal = BigDecimal.ZERO;
        List<QuoteItem> quoteItems = new ArrayList<>();

        if (dto.getItems() != null) {
            for (ExternalQuoteRequestDTO.ExternalQuoteRequestItemDTO itemDto : dto.getItems()) {
                Optional<Product> prodOpt = productRepository.findByTenantIdAndId(tenantId, itemDto.getProductId());
                if (prodOpt.isPresent()) {
                    Product p = prodOpt.get();
                    QuoteItem item = new QuoteItem();
                    item.setProductId(p.getId());
                    item.setDescription(p.getName());
                    item.setQuantity(itemDto.getQuantity());
                    item.setUnitPrice(p.getRentalPrice());
                    BigDecimal itemTotal = p.getRentalPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                    item.setLineSubtotal(itemTotal);
                    item.setLineTotal(itemTotal);
                    subtotal = subtotal.add(itemTotal);
                    quoteItems.add(item);
                }
            }
        }

        quote.setSubtotal(subtotal);
        quote.setTotalAmount(subtotal);
        quote.setQuoteNumber("QTE-EXT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        Quote savedQuote = quoteRepository.save(quote);

        for (QuoteItem item : quoteItems) {
            item.setQuoteId(savedQuote.getId());
            quoteItemRepository.save(item);
        }

        integrationEventService.publishEvent(
            tenantId,
            "quote.created",
            "QUOTE",
            savedQuote.getId().toString(),
            Map.of("quoteId", savedQuote.getId(), "quoteNumber", savedQuote.getQuoteNumber(), "customerId", customer.getId())
        );

        Map<String, Object> result = new HashMap<>();
        result.put("quoteId", savedQuote.getId());
        result.put("quoteNumber", savedQuote.getQuoteNumber());
        result.put("status", savedQuote.getStatus().name());
        result.put("message", "Quote request received and queued for review");
        return result;
    }

    private ExternalCustomerDTO mapCustomer(Customer c) {
        ExternalCustomerDTO dto = new ExternalCustomerDTO();
        dto.setId(c.getId());
        String name = c.getFirstName() != null ? c.getFirstName() + (c.getLastName() != null ? " " + c.getLastName() : "") : c.getCompanyName();
        dto.setName(name);
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setCompany(c.getCompanyName());
        dto.setAddress(c.getBillingAddress());
        dto.setCity(c.getCity());
        dto.setState(c.getState());
        dto.setZipCode(c.getZipCode());
        return dto;
    }

    private ExternalProductDTO mapProduct(Product p) {
        ExternalProductDTO dto = new ExternalProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSku(p.getSku());
        dto.setCategory(p.getCategoryId() != null ? p.getCategoryId().toString() : "GENERAL");
        dto.setDescription(p.getDescription());
        dto.setRentalPrice(p.getRentalPrice());
        dto.setImageUrl(p.getImageUrl());
        dto.setStatus(p.getStatus() != null ? p.getStatus().name() : "ACTIVE");
        dto.setAvailableQuantity(p.getQuantityOwned());
        return dto;
    }

    private ExternalBookingDTO mapBooking(Booking b) {
        ExternalBookingDTO dto = new ExternalBookingDTO();
        dto.setId(b.getId());
        dto.setBookingNumber(b.getBookingNumber());
        dto.setCustomerId(b.getCustomerId());
        dto.setStatus(b.getStatus() != null ? b.getStatus().name() : null);
        dto.setRentalStartDateTime(b.getRentalStartDateTime());
        dto.setRentalEndDateTime(b.getRentalEndDateTime());
        dto.setTotalAmount(b.getTotalAmount());
        dto.setBalanceDue(b.getBalanceDue());
        dto.setCreatedAt(b.getCreatedAt());

        customerRepository.findByTenantIdAndId(b.getTenantId(), b.getCustomerId())
            .ifPresent(c -> {
                String name = c.getFirstName() != null ? c.getFirstName() + (c.getLastName() != null ? " " + c.getLastName() : "") : c.getCompanyName();
                dto.setCustomerName(name);
            });

        List<BookingItem> items = bookingItemRepository.findByBookingId(b.getId());
        dto.setItems(items.stream().map(i -> new ExternalBookingDTO.ExternalBookingItemDTO(
            i.getProductId(), i.getDescription(), i.getQuantity(), i.getUnitPrice(), i.getLineSubtotal()
        )).collect(Collectors.toList()));

        return dto;
    }

    private ExternalInvoiceDTO mapInvoice(Invoice inv) {
        ExternalInvoiceDTO dto = new ExternalInvoiceDTO();
        dto.setId(inv.getId());
        dto.setInvoiceNumber(inv.getInvoiceNumber());
        dto.setBookingId(inv.getBookingId());
        dto.setCustomerId(inv.getCustomerId());
        dto.setStatus(inv.getStatus() != null ? inv.getStatus().name() : null);
        dto.setIssueDate(inv.getIssueDate());
        dto.setDueDate(inv.getDueDate());
        dto.setSubtotal(inv.getSubtotal());
        dto.setDiscount(inv.getDiscount());
        dto.setFees(inv.getFees());
        dto.setTax(inv.getTax());
        dto.setTotalAmount(inv.getTotalAmount());
        dto.setAmountPaid(inv.getAmountPaid());
        dto.setBalanceDue(inv.getBalanceDue());
        dto.setCreatedAt(inv.getCreatedAt());

        customerRepository.findByTenantIdAndId(inv.getTenantId(), inv.getCustomerId())
            .ifPresent(c -> {
                String name = c.getFirstName() != null ? c.getFirstName() + (c.getLastName() != null ? " " + c.getLastName() : "") : c.getCompanyName();
                dto.setCustomerName(name);
            });

        return dto;
    }
}
