package com.rentflow.portal.service;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.dto.CustomerDTO;
import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.ai.service.BookingService;
import com.rentflow.ai.service.CustomerService;
import com.rentflow.ai.service.QuoteService;
import com.rentflow.claims.dto.DamageClaimDTO;
import com.rentflow.claims.service.DamageClaimService;
import com.rentflow.invoice.dto.InvoiceDTO;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.invoice.service.InvoiceService;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.payment.service.PaymentService;
import com.rentflow.portal.dto.*;
import com.rentflow.portal.model.*;
import com.rentflow.portal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StaffCustomerRequestService {

    private final CustomerRequestRepository requestRepository;
    private final CustomerConversationRepository conversationRepository;
    private final QuoteRepository quoteRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final QuoteService quoteService;
    private final BookingService bookingService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final DamageClaimService claimService;
    private final CustomerAddressService addressService;
    private final CustomerMessagingService messagingService;
    private final CustomerActivityRepository activityRepository;

    public StaffCustomerRequestService(CustomerRequestRepository requestRepository,
                                       CustomerConversationRepository conversationRepository,
                                       QuoteRepository quoteRepository,
                                       BookingRepository bookingRepository,
                                       InvoiceRepository invoiceRepository,
                                       CustomerRepository customerRepository,
                                       CustomerService customerService,
                                       QuoteService quoteService,
                                       BookingService bookingService,
                                       InvoiceService invoiceService,
                                       PaymentService paymentService,
                                       DamageClaimService claimService,
                                       CustomerAddressService addressService,
                                       CustomerMessagingService messagingService,
                                       CustomerActivityRepository activityRepository) {
        this.requestRepository = requestRepository;
        this.conversationRepository = conversationRepository;
        this.quoteRepository = quoteRepository;
        this.bookingRepository = bookingRepository;
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.customerService = customerService;
        this.quoteService = quoteService;
        this.bookingService = bookingService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.claimService = claimService;
        this.addressService = addressService;
        this.messagingService = messagingService;
        this.activityRepository = activityRepository;
    }

    public StaffCustomerRequestDashboardDTO getStaffDashboard(String tenantId) {
        List<CustomerRequest> requests = requestRepository.findByTenantIdAndCustomerIdOrderByCreatedAtDesc(tenantId, null);
        List<CustomerConversation> conversations = conversationRepository.findByTenantIdOrderByUpdatedAtDesc(tenantId);
        List<Quote> quotes = quoteRepository.findByTenantId(tenantId);
        List<Booking> bookings = bookingRepository.findByTenantId(tenantId);
        List<Invoice> invoices = invoiceRepository.findByTenantId(tenantId);

        long newQuoteRequests = requests.stream().filter(r -> r.getStatus() == RequestStatus.OPEN).count();
        long pendingQuestions = conversations.stream().filter(c -> c.getStatus() == ConversationStatus.WAITING_FOR_STAFF).count();
        long quotesAwaitingApproval = quotes.stream().filter(q -> q.getStatus() == QuoteStatus.SENT || q.getStatus() == QuoteStatus.CHANGE_REQUESTED).count();
        long upcomingBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long paymentPending = invoices.stream().filter(i -> i.getBalanceDue() != null && i.getBalanceDue().compareTo(BigDecimal.ZERO) > 0).count();
        long openDamageClaims = claimService.getClaims(null, null, null, null, null).size();

        StaffCustomerRequestDashboardDTO dto = new StaffCustomerRequestDashboardDTO();
        dto.setNewQuoteRequestsCount(newQuoteRequests);
        dto.setPendingCustomerQuestionsCount(pendingQuestions);
        dto.setQuotesAwaitingApprovalCount(quotesAwaitingApproval);
        dto.setUpcomingBookingsCount(upcomingBookings);
        dto.setPaymentPendingCount(paymentPending);
        dto.setOpenDamageClaimsCount(openDamageClaims);

        List<StaffCustomerRequestDashboardDTO.RequestItemDTO> items = new ArrayList<>();

        requests.forEach(r -> {
            StaffCustomerRequestDashboardDTO.RequestItemDTO item = new StaffCustomerRequestDashboardDTO.RequestItemDTO();
            item.setId(r.getId());
            item.setRequestNumber("REQ-" + r.getId().toString().substring(0, 6).toUpperCase());
            item.setType(r.getRequestType() != null ? r.getRequestType().name() : "GENERAL");
            item.setEventName(r.getSubject());
            item.setCreatedAt(r.getCreatedAt());
            item.setStatus(r.getStatus() != null ? r.getStatus().name() : "OPEN");

            if (r.getCustomerId() != null) {
                customerRepository.findById(r.getCustomerId()).ifPresent(c -> {
                    item.setCustomerName((c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : "")).trim());
                    item.setCompanyName(c.getCompanyName());
                });
            }
            items.add(item);
        });

        quotes.stream().filter(q -> q.getStatus() == QuoteStatus.SENT || q.getStatus() == QuoteStatus.CHANGE_REQUESTED).forEach(q -> {
            StaffCustomerRequestDashboardDTO.RequestItemDTO item = new StaffCustomerRequestDashboardDTO.RequestItemDTO();
            item.setId(q.getId());
            item.setRequestNumber(q.getQuoteNumber());
            item.setType("QUOTE_AWAITING_APPROVAL");
            item.setEventName("Proposal " + q.getQuoteNumber());
            item.setCreatedAt(q.getCreatedAt());
            item.setStatus(q.getStatus().name());

            if (q.getCustomerId() != null) {
                customerRepository.findById(q.getCustomerId()).ifPresent(c -> {
                    item.setCustomerName((c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : "")).trim());
                    item.setCompanyName(c.getCompanyName());
                });
            }
            items.add(item);
        });

        dto.setRequests(items);
        return dto;
    }

    public Customer360DTO getCustomer360(String tenantId, UUID customerId) {
        Customer360DTO dto = new Customer360DTO();

        // 1. Customer Info
        CustomerDTO customerInfo = customerService.getCustomerById(tenantId, customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + customerId));
        dto.setCustomerInfo(customerInfo);

        // 2. Addresses
        dto.setAddresses(addressService.getCustomerAddresses(tenantId, customerId));

        // 3. Bookings (Upcoming, Active, Past)
        List<BookingDTO> allBookings = bookingService.getBookings(tenantId, "OWNER").stream()
                .filter(b -> customerId.equals(b.getCustomerId()))
                .collect(Collectors.toList());

        List<BookingDTO> upcoming = new ArrayList<>();
        List<BookingDTO> active = new ArrayList<>();
        List<BookingDTO> past = new ArrayList<>();

        for (BookingDTO b : allBookings) {
            if (b.getStatus() == BookingStatus.IN_PROGRESS || b.getStatus() == BookingStatus.DELIVERED) {
                active.add(b);
            } else if (b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.CANCELLED) {
                past.add(b);
            } else {
                upcoming.add(b);
            }
        }
        dto.setUpcomingBookings(upcoming);
        dto.setActiveRentals(active);
        dto.setPastBookings(past);

        // 4. Quotes
        List<QuoteDTO> quotes = quoteService.getQuotes(tenantId, "OWNER").stream()
                .filter(q -> customerId.equals(q.getCustomerId()))
                .collect(Collectors.toList());
        dto.setQuotes(quotes);

        // 5. Invoices & Payments
        List<Invoice> invoices = invoiceRepository.findByTenantIdAndCustomerId(tenantId, customerId);
        List<InvoiceDTO> invoiceDTOs = invoices.stream().map(i -> {
            InvoiceDTO idto = new InvoiceDTO();
            idto.setId(i.getId());
            idto.setInvoiceNumber(i.getInvoiceNumber());
            idto.setBookingId(i.getBookingId());
            idto.setCustomerId(i.getCustomerId());
            idto.setTotalAmount(i.getTotalAmount());
            idto.setAmountPaid(i.getAmountPaid());
            idto.setBalanceDue(i.getBalanceDue());
            idto.setStatus(i.getStatus());
            return idto;
        }).collect(Collectors.toList());
        dto.setInvoices(invoiceDTOs);

        List<PaymentDTO> payments = new ArrayList<>();
        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal balanceDue = BigDecimal.ZERO;

        for (Invoice inv : invoices) {
            if (inv.getBookingId() != null) {
                payments.addAll(paymentService.getBookingPayments(tenantId, inv.getBookingId()));
            }
            if (inv.getAmountPaid() != null) totalSpent = totalSpent.add(inv.getAmountPaid());
            if (inv.getBalanceDue() != null) balanceDue = balanceDue.add(inv.getBalanceDue());
        }
        dto.setPayments(payments);
        dto.setTotalLifetimeValue(totalSpent);
        dto.setCurrentOutstandingBalance(balanceDue);

        // 6. Claims
        List<DamageClaimDTO> claims = claimService.getClaims(null, null, null, customerId, null);
        dto.setDamageClaims(claims);

        // 7. Messages
        dto.setMessages(messagingService.getCustomerConversations(tenantId, customerId));

        // 8. Activity Timeline
        dto.setActivityTimeline(activityRepository.findByTenantIdAndCustomerIdOrderByCreatedAtDesc(tenantId, customerId));

        return dto;
    }
}
