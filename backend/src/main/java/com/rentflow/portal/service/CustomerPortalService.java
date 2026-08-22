package com.rentflow.portal.service;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.invoice.dto.InvoiceDTO;
import com.rentflow.invoice.dto.InvoiceItemDTO;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.repository.InvoiceItemRepository;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.payment.service.PaymentService;
import com.rentflow.portal.dto.*;
import com.rentflow.portal.model.*;
import com.rentflow.portal.repository.*;
import com.rentflow.role.RoleType;

import com.rentflow.notification.event.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerPortalService {

    private final CustomerUserRepository customerUserRepository;
    private final CustomerRequestRepository customerRequestRepository;
    private final CustomerRepository customerRepository;
    private final EventRepository eventRepository;
    private final EventRequirementRepository requirementRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;

    public CustomerPortalService(CustomerUserRepository customerUserRepository,
                                CustomerRequestRepository customerRequestRepository,
                                CustomerRepository customerRepository,
                                EventRepository eventRepository,
                                EventRequirementRepository requirementRepository,
                                QuoteRepository quoteRepository,
                                QuoteItemRepository quoteItemRepository,
                                BookingRepository bookingRepository,
                                BookingItemRepository bookingItemRepository,
                                InvoiceRepository invoiceRepository,
                                InvoiceItemRepository invoiceItemRepository,
                                PaymentService paymentService,
                                ApplicationEventPublisher eventPublisher) {
        this.customerUserRepository = customerUserRepository;
        this.customerRequestRepository = customerRequestRepository;
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
        this.requirementRepository = requirementRepository;
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.paymentService = paymentService;
        this.eventPublisher = eventPublisher;
    }

    public CustomerAuthResponseDTO login(String email, String password) {
        Optional<CustomerUser> cuOpt = customerUserRepository.findByEmailIgnoreCase(email);
        if (cuOpt.isEmpty() || !cuOpt.get().isActive()) {
            throw new IllegalArgumentException("Invalid credentials or customer user account inactive.");
        }
        CustomerUser cu = cuOpt.get();
        if (!cu.getPasswordHash().equals(password)) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        Customer customer = customerRepository.findById(cu.getCustomerId())
                .orElseGet(() -> {
                    Customer fallback = new Customer();
                    fallback.setId(cu.getCustomerId());
                    fallback.setTenantId(cu.getTenantId());
                    fallback.setCustomerNumber("CUS-AUTO-" + UUID.randomUUID().toString().substring(0, 6));
                    fallback.setFirstName("Emily");
                    fallback.setLastName("Brown");
                    fallback.setCompanyName("ABC Events LLC");
                    fallback.setEmail(cu.getEmail());
                    return customerRepository.save(fallback);
                });

        CustomerAuthResponseDTO res = new CustomerAuthResponseDTO();
        res.setToken("demo-portal-token-" + cu.getId());
        res.setUserId(cu.getUserId());
        res.setCustomerId(cu.getCustomerId());
        res.setTenantId(cu.getTenantId());
        res.setEmail(cu.getEmail());
        res.setCustomerName((customer.getFirstName() + " " + customer.getLastName()).trim());
        res.setCompanyName(customer.getCompanyName());
        res.setRole("CUSTOMER");
        return res;
    }

    @Transactional(readOnly = true)
    public CustomerPortalDashboardDTO getDashboard(String tenantId, UUID customerId) {
        Customer customer = getCustomerWithAuth(tenantId, customerId);

        List<Event> events = eventRepository.findByTenantIdAndCustomerId(tenantId, customerId);
        List<Quote> quotes = quoteRepository.findByTenantIdAndCustomerId(tenantId, customerId);
        List<Booking> bookings = bookingRepository.findByTenantIdAndCustomerId(tenantId, customerId);
        List<Invoice> invoices = invoiceRepository.findByTenantIdAndCustomerId(tenantId, customerId);

        CustomerPortalEventDTO upcomingEvent = events.stream()
                .filter(e -> e.getEventDate() != null && !e.getEventDate().isBefore(LocalDate.now()))
                .min(Comparator.comparing(Event::getEventDate))
                .map(e -> mapEventToDTO(tenantId, e))
                .orElse(events.isEmpty() ? null : mapEventToDTO(tenantId, events.get(0)));

        long activeQuotesCount = quotes.stream()
                .filter(q -> q.getStatus() != QuoteStatus.DECLINED && q.getStatus() != QuoteStatus.EXPIRED && q.getStatus() != QuoteStatus.REJECTED)
                .count();

        long activeBookingsCount = bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .count();

        BigDecimal outstandingBalance = invoices.stream()
                .filter(i -> i.getStatus() != com.rentflow.invoice.model.InvoiceStatus.VOID)
                .map(Invoice::getBalanceDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CustomerPortalDashboardDTO.RecentActivityDTO> activities = new ArrayList<>();

        quotes.forEach(q -> activities.add(new CustomerPortalDashboardDTO.RecentActivityDTO(
                "QUOTE", "Quote " + q.getQuoteNumber(), "Status: " + q.getStatus().name(),
                q.getQuoteDate() != null ? q.getQuoteDate().toString() : LocalDate.now().toString()
        )));

        invoices.forEach(i -> activities.add(new CustomerPortalDashboardDTO.RecentActivityDTO(
                "INVOICE", "Invoice " + i.getInvoiceNumber(), "Status: " + i.getStatus().name() + " • Balance: $" + i.getBalanceDue(),
                i.getIssueDate() != null ? i.getIssueDate().toString() : LocalDate.now().toString()
        )));

        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        CustomerPortalDashboardDTO dto = new CustomerPortalDashboardDTO();
        dto.setCustomerName((customer.getFirstName() + " " + customer.getLastName()).trim());
        dto.setCompanyName(customer.getCompanyName());
        dto.setUpcomingEvent(upcomingEvent);
        dto.setActiveQuotesCount(activeQuotesCount);
        dto.setActiveBookingsCount(activeBookingsCount);
        dto.setInvoicesCount(invoices.size());
        dto.setOutstandingBalance(outstandingBalance);
        dto.setRecentActivities(activities.stream().limit(5).collect(Collectors.toList()));
        return dto;
    }

    @Transactional(readOnly = true)
    public CustomerProfileDTO getProfile(String tenantId, UUID customerId) {
        Customer customer = getCustomerWithAuth(tenantId, customerId);
        return mapCustomerToProfileDTO(customer);
    }

    public CustomerProfileDTO updateProfile(String tenantId, UUID customerId, CustomerProfileDTO dto) {
        Customer customer = getCustomerWithAuth(tenantId, customerId);
        if (dto.getFirstName() != null) customer.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) customer.setLastName(dto.getLastName());
        if (dto.getCompanyName() != null) customer.setCompanyName(dto.getCompanyName());
        if (dto.getPhone() != null) customer.setPhone(dto.getPhone());
        if (dto.getAlternatePhone() != null) customer.setAlternatePhone(dto.getAlternatePhone());
        if (dto.getBillingAddress() != null) customer.setBillingAddress(dto.getBillingAddress());
        if (dto.getShippingAddress() != null) customer.setShippingAddress(dto.getShippingAddress());
        if (dto.getCity() != null) customer.setCity(dto.getCity());
        if (dto.getState() != null) customer.setState(dto.getState());
        if (dto.getZipCode() != null) customer.setZipCode(dto.getZipCode());
        if (dto.getCountry() != null) customer.setCountry(dto.getCountry());

        Customer saved = customerRepository.save(customer);
        return mapCustomerToProfileDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerPortalEventDTO> getCustomerEvents(String tenantId, UUID customerId) {
        getCustomerWithAuth(tenantId, customerId);
        return eventRepository.findByTenantIdAndCustomerId(tenantId, customerId).stream()
                .map(e -> mapEventToDTO(tenantId, e))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerPortalEventDTO getEventDetail(String tenantId, UUID customerId, UUID eventId) {
        getCustomerWithAuth(tenantId, customerId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found."));
        verifyOwnership(tenantId, customerId, event.getTenantId(), event.getCustomerId(), "Event");
        return mapEventToDTO(tenantId, event);
    }

    @Transactional(readOnly = true)
    public List<CustomerPortalQuoteDTO> getCustomerQuotes(String tenantId, UUID customerId) {
        getCustomerWithAuth(tenantId, customerId);
        return quoteRepository.findByTenantIdAndCustomerId(tenantId, customerId).stream()
                .map(q -> mapQuoteToDTO(tenantId, q))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerPortalQuoteDTO getQuoteDetail(String tenantId, UUID customerId, UUID quoteId) {
        getCustomerWithAuth(tenantId, customerId);
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new SecurityException("Access Denied: Quote not found or unauthorized."));
        verifyOwnership(tenantId, customerId, quote.getTenantId(), quote.getCustomerId(), "Quote");

        return mapQuoteToDTO(tenantId, quote);
    }

    public CustomerPortalQuoteDTO acceptQuote(String tenantId, UUID customerId, UUID quoteId, String role) {
        getCustomerWithAuth(tenantId, customerId);
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new SecurityException("Access Denied: Quote not found or unauthorized."));
        verifyOwnership(tenantId, customerId, quote.getTenantId(), quote.getCustomerId(), "Quote");

        if (quote.getStatus() == QuoteStatus.ACCEPTED) {
            throw new IllegalStateException("Quote is already accepted.");
        }
        if (quote.getValidUntil() != null && quote.getValidUntil().isBefore(LocalDate.now())) {
            quote.setStatus(QuoteStatus.EXPIRED);
            quoteRepository.save(quote);
            throw new IllegalStateException("Quote has expired and cannot be accepted.");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        Quote saved = quoteRepository.save(quote);

        Customer customer = getCustomerWithAuth(tenantId, customerId);
        String custName = customer.getCompanyName() != null && !customer.getCompanyName().isBlank() ? customer.getCompanyName() : customer.getFirstName();
        eventPublisher.publishEvent(new QuoteAcceptedEvent(tenantId, quoteId, saved.getQuoteNumber(), customerId, custName, saved.getTotalAmount()));

        return mapQuoteToDTO(tenantId, saved);
    }

    public CustomerPortalQuoteDTO requestQuoteChanges(String tenantId, UUID customerId, UUID quoteId, String message, String role) {
        Customer customer = getCustomerWithAuth(tenantId, customerId);
        String custName = customer.getCompanyName() != null && !customer.getCompanyName().isBlank() ? customer.getCompanyName() : customer.getFirstName();
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new SecurityException("Access Denied: Quote not found or unauthorized."));
        verifyOwnership(tenantId, customerId, quote.getTenantId(), quote.getCustomerId(), "Quote");

        quote.setStatus(QuoteStatus.CHANGE_REQUESTED);
        Quote saved = quoteRepository.save(quote);

        CustomerRequest req = new CustomerRequest();
        req.setTenantId(tenantId);
        req.setCustomerId(customerId);
        req.setQuoteId(quoteId);
        req.setRequestType(RequestType.QUOTE_CHANGE);
        req.setSubject("Change Request for Quote " + quote.getQuoteNumber());
        req.setMessage(message != null ? message : "Customer requested changes for quote.");
        req.setStatus(RequestStatus.OPEN);
        customerRequestRepository.save(req);

        eventPublisher.publishEvent(new QuoteChangeRequestedEvent(tenantId, quoteId, saved.getQuoteNumber(), customerId, custName, req.getMessage()));

        return mapQuoteToDTO(tenantId, saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerPortalBookingDTO> getCustomerBookings(String tenantId, UUID customerId) {
        getCustomerWithAuth(tenantId, customerId);
        return bookingRepository.findByTenantIdAndCustomerId(tenantId, customerId).stream()
                .map(b -> mapBookingToDTO(tenantId, b))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerPortalBookingDTO getBookingDetail(String tenantId, UUID customerId, UUID bookingId) {
        getCustomerWithAuth(tenantId, customerId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new SecurityException("Access Denied: Booking not found or unauthorized."));
        verifyOwnership(tenantId, customerId, booking.getTenantId(), booking.getCustomerId(), "Booking");

        return mapBookingToDTO(tenantId, booking);
    }

    @Transactional(readOnly = true)
    public List<CustomerPortalInvoiceDTO> getCustomerInvoices(String tenantId, UUID customerId) {
        getCustomerWithAuth(tenantId, customerId);
        return invoiceRepository.findByTenantIdAndCustomerId(tenantId, customerId).stream()
                .map(i -> mapInvoiceToDTO(i))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerPortalInvoiceDTO getInvoiceDetail(String tenantId, UUID customerId, UUID invoiceId) {
        getCustomerWithAuth(tenantId, customerId);
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new SecurityException("Access Denied: Invoice not found or unauthorized."));
        verifyOwnership(tenantId, customerId, invoice.getTenantId(), invoice.getCustomerId(), "Invoice");

        return mapInvoiceToDTO(invoice);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getInvoicePayments(String tenantId, UUID customerId, UUID invoiceId) {
        CustomerPortalInvoiceDTO inv = getInvoiceDetail(tenantId, customerId, invoiceId);
        if (inv.getBookingId() == null) {
            return Collections.emptyList();
        }
        return paymentService.getBookingPayments(tenantId, inv.getBookingId());
    }

    @Transactional(readOnly = true)
    public List<CustomerRequestDTO> getCustomerRequests(String tenantId, UUID customerId) {
        getCustomerWithAuth(tenantId, customerId);
        return customerRequestRepository.findByTenantIdAndCustomerIdOrderByCreatedAtDesc(tenantId, customerId).stream()
                .map(this::mapRequestToDTO)
                .collect(Collectors.toList());
    }

    public CustomerRequestDTO createCustomerRequest(String tenantId, UUID customerId, CreateCustomerRequestDTO dto) {
        Customer customer = getCustomerWithAuth(tenantId, customerId);

        CustomerRequest req = new CustomerRequest();
        req.setTenantId(tenantId);
        req.setCustomerId(customerId);
        req.setRequestType(dto.getType() != null ? dto.getType() : RequestType.GENERAL);
        req.setSubject(dto.getSubject());
        req.setMessage(dto.getMessage());
        req.setQuoteId(dto.getQuoteId());
        req.setBookingId(dto.getBookingId());
        req.setStatus(RequestStatus.OPEN);

        CustomerRequest saved = customerRequestRepository.save(req);

        String custName = customer.getCompanyName() != null && !customer.getCompanyName().isBlank() ? customer.getCompanyName() : customer.getFirstName();
        eventPublisher.publishEvent(new CustomerRequestCreatedEvent(tenantId, saved.getId(), customerId, custName, saved.getRequestType().name(), saved.getSubject(), saved.getMessage()));

        return mapRequestToDTO(saved);
    }

    // Helper Authorization & Mapping Methods

    private Customer getCustomerWithAuth(String tenantId, UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseGet(() -> {
                    Customer fallback = new Customer();
                    fallback.setId(customerId);
                    fallback.setTenantId("99999999-9999-9999-9999-999999999999");
                    fallback.setCustomerNumber("CUS-AUTO-" + UUID.randomUUID().toString().substring(0, 6));
                    fallback.setFirstName("Valued");
                    fallback.setLastName("Customer");
                    fallback.setCompanyName("Demo Events LLC");
                    fallback.setEmail("customer@demo.com");
                    return customerRepository.save(fallback);
                });
        if (!customer.getTenantId().equals(tenantId)) {
            throw new SecurityException("Access Denied: Tenant mismatch.");
        }
        return customer;
    }

    private void verifyOwnership(String userTenantId, UUID userCustomerId, String resourceTenantId, UUID resourceCustomerId, String resourceName) {
        if (!resourceTenantId.equals(userTenantId) || !resourceCustomerId.equals(userCustomerId)) {
            throw new SecurityException("Access Denied: You do not have permission to access this " + resourceName + ".");
        }
    }

    private CustomerProfileDTO mapCustomerToProfileDTO(Customer c) {
        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setCustomerId(c.getId());
        dto.setCustomerNumber(c.getCustomerNumber());
        dto.setFirstName(c.getFirstName());
        dto.setLastName(c.getLastName());
        dto.setCompanyName(c.getCompanyName());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setAlternatePhone(c.getAlternatePhone());
        dto.setBillingAddress(c.getBillingAddress());
        dto.setShippingAddress(c.getShippingAddress());
        dto.setCity(c.getCity());
        dto.setState(c.getState());
        dto.setZipCode(c.getZipCode());
        dto.setCountry(c.getCountry());
        return dto;
    }

    private CustomerPortalEventDTO mapEventToDTO(String tenantId, Event e) {
        CustomerPortalEventDTO dto = new CustomerPortalEventDTO();
        dto.setId(e.getId());
        dto.setEventName(e.getEventName());
        dto.setEventType(e.getEventType() != null ? e.getEventType().name() : "EVENT");
        dto.setEventDate(e.getEventDate());
        dto.setStartTime(e.getStartTime());
        dto.setEndTime(e.getEndTime());
        dto.setVenueName(e.getVenueName());
        dto.setVenueAddress(e.getVenueAddress());
        dto.setCity(e.getCity());
        dto.setState(e.getState());
        dto.setZipCode(e.getZipCode());
        dto.setStatus(e.getStatus() != null ? e.getStatus().name() : "PLANNING");

        List<EventRequirement> reqs = requirementRepository.findByTenantIdAndEventId(tenantId, e.getId());
        dto.setRequirements(reqs.stream()
                .map(r -> r.getQuantity() + "x " + r.getDescription())
                .collect(Collectors.toList()));

        return dto;
    }

    private CustomerPortalQuoteDTO mapQuoteToDTO(String tenantId, Quote q) {
        CustomerPortalQuoteDTO dto = new CustomerPortalQuoteDTO();
        dto.setId(q.getId());
        dto.setQuoteNumber(q.getQuoteNumber());

        if (q.getEventId() != null) {
            eventRepository.findById(q.getEventId()).ifPresent(e -> dto.setEventName(e.getEventName()));
        }
        if (dto.getEventName() == null) dto.setEventName("Proposal for Customer");

        dto.setQuoteDate(q.getQuoteDate());
        dto.setValidUntil(q.getValidUntil());
        dto.setRentalStartDateTime(q.getRentalStartDateTime());
        dto.setRentalEndDateTime(q.getRentalEndDateTime());
        dto.setSubtotal(q.getSubtotal());
        dto.setDiscountAmount(q.getDiscountAmount());
        dto.setFees((q.getDeliveryFee() != null ? q.getDeliveryFee() : BigDecimal.ZERO)
                .add(q.getPickupFee() != null ? q.getPickupFee() : BigDecimal.ZERO)
                .add(q.getSetupFee() != null ? q.getSetupFee() : BigDecimal.ZERO));
        dto.setTaxAmount(q.getTaxAmount());
        dto.setTotalAmount(q.getTotalAmount());
        dto.setDepositRequired(q.getDepositAmount());
        dto.setStatus(q.getStatus() != null ? q.getStatus().name() : "SENT");
        dto.setNotes(q.getNotes());

        List<QuoteItem> items = quoteItemRepository.findByQuoteId(q.getId());
        dto.setItems(items.stream().map(i -> {
            CustomerPortalQuoteDTO.CustomerPortalQuoteItemDTO itemDto = new CustomerPortalQuoteDTO.CustomerPortalQuoteItemDTO();
            itemDto.setId(i.getId());
            itemDto.setDescription(i.getDescription());
            itemDto.setQuantity(i.getQuantity());
            itemDto.setUnitPrice(i.getUnitPrice());
            itemDto.setLineSubtotal(i.getLineSubtotal());
            return itemDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    private CustomerPortalBookingDTO mapBookingToDTO(String tenantId, Booking b) {
        CustomerPortalBookingDTO dto = new CustomerPortalBookingDTO();
        dto.setId(b.getId());
        dto.setBookingNumber(b.getBookingNumber());

        if (b.getEventId() != null) {
            eventRepository.findById(b.getEventId()).ifPresent(e -> {
                dto.setEventName(e.getEventName());
                dto.setVenueName(e.getVenueName());
                dto.setVenueAddress(e.getVenueAddress());
            });
        }
        if (dto.getEventName() == null) dto.setEventName("Event Booking");

        dto.setBookingDate(b.getBookingDate());
        dto.setRentalStartDateTime(b.getRentalStartDateTime());
        dto.setRentalEndDateTime(b.getRentalEndDateTime());
        dto.setSubtotal(b.getSubtotal());
        dto.setDiscountAmount(b.getDiscountAmount());
        dto.setFees((b.getDeliveryFee() != null ? b.getDeliveryFee() : BigDecimal.ZERO)
                .add(b.getPickupFee() != null ? b.getPickupFee() : BigDecimal.ZERO)
                .add(b.getSetupFee() != null ? b.getSetupFee() : BigDecimal.ZERO)
                .add(b.getBreakdownFee() != null ? b.getBreakdownFee() : BigDecimal.ZERO)
                .add(b.getServiceFee() != null ? b.getServiceFee() : BigDecimal.ZERO));
        dto.setTaxAmount(b.getTaxAmount());
        dto.setTotalAmount(b.getTotalAmount());
        dto.setDepositPaid(b.getDepositPaid());
        dto.setBalanceDue(b.getBalanceDue());
        dto.setStatus(b.getStatus() != null ? b.getStatus().name() : "CONFIRMED");
        dto.setNotes(b.getNotes());

        List<BookingItem> items = bookingItemRepository.findByBookingId(b.getId());
        dto.setItems(items.stream().map(i -> {
            CustomerPortalBookingDTO.CustomerPortalBookingItemDTO itemDto = new CustomerPortalBookingDTO.CustomerPortalBookingItemDTO();
            itemDto.setId(i.getId());
            itemDto.setDescription(i.getDescription());
            itemDto.setQuantity(i.getQuantity());
            itemDto.setUnitPrice(i.getUnitPrice());
            itemDto.setLineSubtotal(i.getLineSubtotal());
            return itemDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    private CustomerPortalInvoiceDTO mapInvoiceToDTO(Invoice i) {
        CustomerPortalInvoiceDTO dto = new CustomerPortalInvoiceDTO();
        dto.setId(i.getId());
        dto.setInvoiceNumber(i.getInvoiceNumber());
        dto.setBookingId(i.getBookingId());
        dto.setCustomerId(i.getCustomerId());
        dto.setCustomerName(i.getCustomerName());
        dto.setCompanyName(i.getCompanyName());
        dto.setEmail(i.getEmail());
        dto.setPhone(i.getPhone());
        dto.setBillingAddress(i.getBillingAddress());
        dto.setCity(i.getCity());
        dto.setState(i.getState());
        dto.setZipCode(i.getZipCode());
        dto.setCountry(i.getCountry());
        dto.setIssueDate(i.getIssueDate());
        dto.setDueDate(i.getDueDate());
        dto.setSubtotal(i.getSubtotal());
        dto.setDiscount(i.getDiscount());
        dto.setFees(i.getFees());
        dto.setTax(i.getTax());
        dto.setTotalAmount(i.getTotalAmount());
        dto.setAmountPaid(i.getAmountPaid());
        dto.setBalanceDue(i.getBalanceDue());
        dto.setStatus(i.getStatus() != null ? i.getStatus().name() : "DRAFT");
        dto.setNotes(i.getNotes());

        List<com.rentflow.invoice.model.InvoiceItem> items = invoiceItemRepository.findByInvoiceId(i.getId());
        dto.setItems(items.stream().map(item -> {
            InvoiceItemDTO itemDto = new InvoiceItemDTO();
            itemDto.setId(item.getId());
            itemDto.setDescription(item.getDescription());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setDiscount(item.getDiscount());
            itemDto.setTax(item.getTax());
            itemDto.setLineTotal(item.getLineTotal());
            return itemDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    private CustomerRequestDTO mapRequestToDTO(CustomerRequest r) {
        CustomerRequestDTO dto = new CustomerRequestDTO();
        dto.setId(r.getId());
        dto.setRequestType(r.getRequestType());
        dto.setSubject(r.getSubject());
        dto.setMessage(r.getMessage());
        dto.setStatus(r.getStatus());
        dto.setQuoteId(r.getQuoteId());
        dto.setBookingId(r.getBookingId());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
