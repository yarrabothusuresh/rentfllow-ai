package com.rentflow.analytics.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingItem;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Quote;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.repository.BookingItemRepository;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.analytics.dto.*;
import com.rentflow.claims.model.DamageClaim;
import com.rentflow.claims.repository.DamageClaimRepository;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.payment.model.Payment;
import com.rentflow.payment.model.PaymentStatus;
import com.rentflow.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerAnalyticsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DamageClaimRepository damageClaimRepository;

    @Autowired
    private RevenueAnalyticsService revenueAnalyticsService;

    @Autowired
    private ProfitabilityService profitabilityService;

    public List<CustomerAnalyticsDTO> getCustomerAnalyticsList(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        List<Customer> customers = customerRepository.findByTenantId(tenantId);
        List<Booking> allBookings = bookingRepository.findByTenantId(tenantId);
        List<Invoice> allInvoices = invoiceRepository.findByTenantId(tenantId);
        List<Payment> allPayments = paymentRepository.findByTenantId(tenantId);
        List<Quote> allQuotes = quoteRepository.findByTenantId(tenantId);
        List<DamageClaim> allClaims = damageClaimRepository.findByTenantId(tenantId);

        List<CustomerAnalyticsDTO> list = new ArrayList<>();
        for (Customer c : customers) {
            CustomerAnalyticsDTO dto = new CustomerAnalyticsDTO();
            dto.setCustomerId(c.getId());
            dto.setCustomerNumber(c.getCustomerNumber());
            String cName = ((c.getFirstName() != null ? c.getFirstName() : "") + " " + (c.getLastName() != null ? c.getLastName() : "")).trim();
            dto.setCustomerName(cName);
            dto.setCompanyName(c.getCompanyName());
            dto.setCustomerType(c.getCustomerType() != null ? c.getCustomerType().name() : "STANDARD");

            List<Booking> custBookings = allBookings.stream()
                    .filter(b -> c.getId().equals(b.getCustomerId()) && b.getStatus() != BookingStatus.CANCELLED)
                    .collect(Collectors.toList());

            dto.setTotalBookingsCount(custBookings.size());

            BigDecimal totalRev = custBookings.stream()
                    .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setLifetimeRevenue(totalRev);

            // Collected from payments
            BigDecimal collected = allPayments.stream()
                    .filter(p -> c.getId().equals(p.getCustomerId()) && p.getPaymentStatus() == PaymentStatus.COMPLETED)
                    .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setLifetimeCollected(collected);

            // Outstanding balance from unpaid invoices
            BigDecimal outstanding = allInvoices.stream()
                    .filter(i -> c.getId().equals(i.getCustomerId()) && i.getStatus() != InvoiceStatus.VOID && i.getStatus() != InvoiceStatus.PAID)
                    .map(i -> i.getBalanceDue() != null ? i.getBalanceDue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setOutstandingBalance(outstanding);

            // Lifetime Profit (approx 45% margin baseline)
            BigDecimal profit = totalRev.multiply(new BigDecimal("0.45")).setScale(2, RoundingMode.HALF_UP);
            dto.setLifetimeProfit(profit);

            BigDecimal margin = BigDecimal.ZERO;
            if (totalRev.compareTo(BigDecimal.ZERO) > 0) {
                margin = profit.multiply(new BigDecimal("100")).divide(totalRev, 2, RoundingMode.HALF_UP);
            }
            dto.setLifetimeMarginPercent(margin);

            if (!custBookings.isEmpty()) {
                dto.setAverageBookingValue(totalRev.divide(BigDecimal.valueOf(custBookings.size()), 2, RoundingMode.HALF_UP));
                custBookings.stream()
                        .map(Booking::getBookingDate)
                        .filter(Objects::nonNull)
                        .max(LocalDate::compareTo)
                        .ifPresent(dto::setLastBookingDate);
            }

            // Quote conversion rate
            List<Quote> custQuotes = allQuotes.stream()
                    .filter(q -> c.getId().equals(q.getCustomerId()))
                    .collect(Collectors.toList());
            if (!custQuotes.isEmpty()) {
                long approved = custQuotes.stream().filter(q -> q.getStatus() == QuoteStatus.ACCEPTED).count();
                dto.setQuoteConversionRate(BigDecimal.valueOf(approved).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(custQuotes.size()), 2, RoundingMode.HALF_UP));
            }

            // Damage claims count
            long claimsCount = allClaims.stream().filter(dc -> c.getId().equals(dc.getCustomerId())).count();
            dto.setDamageClaimsCount((int) claimsCount);

            list.add(dto);
        }

        list.sort((a, b) -> b.getLifetimeRevenue().compareTo(a.getLifetimeRevenue()));
        return list;
    }

    public CustomerAnalyticsDetailDTO getCustomerAnalyticsDetail(String tenantId, UUID customerId) {
        Customer c = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        if (!tenantId.equals(c.getTenantId())) {
            throw new IllegalArgumentException("Unauthorized tenant access");
        }

        CustomerAnalyticsDetailDTO dto = new CustomerAnalyticsDetailDTO();
        dto.setCustomerId(c.getId());
        dto.setCustomerNumber(c.getCustomerNumber());
        String cName = ((c.getFirstName() != null ? c.getFirstName() : "") + " " + (c.getLastName() != null ? c.getLastName() : "")).trim();
        dto.setCustomerName(cName);
        dto.setCompanyName(c.getCompanyName());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setCustomerType(c.getCustomerType() != null ? c.getCustomerType().name() : "STANDARD");

        List<Booking> bookings = bookingRepository.findByTenantId(tenantId).stream()
                .filter(b -> c.getId().equals(b.getCustomerId()) && b.getStatus() != BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        dto.setTotalBookingsCount(bookings.size());

        BigDecimal totalRev = bookings.stream()
                .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setLifetimeRevenue(totalRev);

        BigDecimal collected = paymentRepository.findByTenantId(tenantId).stream()
                .filter(p -> c.getId().equals(p.getCustomerId()) && p.getPaymentStatus() == PaymentStatus.COMPLETED)
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setLifetimeCollected(collected);

        BigDecimal outstanding = invoiceRepository.findByTenantId(tenantId).stream()
                .filter(i -> c.getId().equals(i.getCustomerId()) && i.getStatus() != InvoiceStatus.VOID && i.getStatus() != InvoiceStatus.PAID)
                .map(i -> i.getBalanceDue() != null ? i.getBalanceDue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setOutstandingBalance(outstanding);

        BigDecimal profit = totalRev.multiply(new BigDecimal("0.45")).setScale(2, RoundingMode.HALF_UP);
        dto.setLifetimeProfit(profit);

        BigDecimal margin = BigDecimal.ZERO;
        if (totalRev.compareTo(BigDecimal.ZERO) > 0) {
            margin = profit.multiply(new BigDecimal("100")).divide(totalRev, 2, RoundingMode.HALF_UP);
        }
        dto.setLifetimeMarginPercent(margin);

        if (!bookings.isEmpty()) {
            dto.setAverageBookingValue(totalRev.divide(BigDecimal.valueOf(bookings.size()), 2, RoundingMode.HALF_UP));
            bookings.stream()
                    .map(Booking::getBookingDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .ifPresent(dto::setLastBookingDate);
        }

        List<Quote> quotes = quoteRepository.findByTenantId(tenantId).stream()
                .filter(q -> c.getId().equals(q.getCustomerId()))
                .collect(Collectors.toList());
        dto.setTotalQuotesCount(quotes.size());
        long approvedQ = quotes.stream().filter(q -> q.getStatus() == QuoteStatus.ACCEPTED).count();
        dto.setApprovedQuotesCount((int) approvedQ);
        if (!quotes.isEmpty()) {
            dto.setQuoteConversionRate(BigDecimal.valueOf(approvedQ).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(quotes.size()), 2, RoundingMode.HALF_UP));
        }

        List<DamageClaim> claims = damageClaimRepository.findByTenantId(tenantId).stream()
                .filter(dc -> c.getId().equals(dc.getCustomerId()))
                .collect(Collectors.toList());
        dto.setDamageClaimsCount(claims.size());
        BigDecimal dmgCost = claims.stream()
                .map(dc -> dc.getFinalTotalCost() != null ? dc.getFinalTotalCost() : (dc.getEstimatedTotalCost() != null ? dc.getEstimatedTotalCost() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalDamageCost(dmgCost);

        // Top rented products for this customer
        Map<String, BigDecimal> productRevMap = new HashMap<>();
        Map<String, Integer> productQtyMap = new HashMap<>();
        for (Booking b : bookings) {
            List<BookingItem> bItems = bookingItemRepository.findByBookingId(b.getId());
            for (BookingItem bi : bItems) {
                String pName = bi.getDescription() != null ? bi.getDescription() : "Product";
                BigDecimal amt = bi.getLineSubtotal() != null ? bi.getLineSubtotal() : BigDecimal.ZERO;
                productRevMap.put(pName, productRevMap.getOrDefault(pName, BigDecimal.ZERO).add(amt));
                productQtyMap.put(pName, productQtyMap.getOrDefault(pName, 0) + bi.getQuantity());
            }
        }

        List<NamedMetricDTO> topProds = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : productRevMap.entrySet()) {
            topProds.add(new NamedMetricDTO(entry.getKey(), entry.getValue(), productQtyMap.getOrDefault(entry.getKey(), 0), BigDecimal.ZERO));
        }
        topProds.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        dto.setTopRentedProducts(topProds.stream().limit(5).collect(Collectors.toList()));

        // Revenue trend for this customer
        dto.setRevenueTrend(generateCustomerRevenueTrend(bookings));

        // Recent bookings summary
        List<BookingProfitabilityDTO> recent = new ArrayList<>();
        for (Booking b : bookings.stream().limit(10).collect(Collectors.toList())) {
            BookingProfitabilityDTO bp = new BookingProfitabilityDTO();
            bp.setBookingId(b.getId());
            bp.setBookingNumber(b.getBookingNumber());
            bp.setStatus(b.getStatus().name());
            bp.setBookingDate(b.getBookingDate());
            bp.setTotalRevenue(b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO);
            BigDecimal dirCost = bp.getTotalRevenue().multiply(new BigDecimal("0.35")).setScale(2, RoundingMode.HALF_UP);
            bp.setDirectCost(dirCost);
            bp.setGrossProfit(bp.getTotalRevenue().subtract(dirCost));
            bp.setGrossMarginPercent(bp.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0 ? bp.getGrossProfit().multiply(new BigDecimal("100")).divide(bp.getTotalRevenue(), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            bp.setMarginFlag(profitabilityService.determineMarginFlag(bp.getGrossMarginPercent(), bp.getGrossProfit()));
            recent.add(bp);
        }
        dto.setRecentBookings(recent);

        return dto;
    }

    private List<RevenueTrendItemDTO> generateCustomerRevenueTrend(List<Booking> bookings) {
        LocalDate today = LocalDate.now();
        List<RevenueTrendItemDTO> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 5; i >= 0; i--) {
            LocalDate monthTarget = today.minusMonths(i);
            LocalDate start = monthTarget.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate end = monthTarget.with(TemporalAdjusters.lastDayOfMonth());
            String periodKey = monthTarget.format(fmt);

            BigDecimal rev = bookings.stream()
                    .filter(b -> b.getBookingDate() != null && !b.getBookingDate().isBefore(start) && !b.getBookingDate().isAfter(end))
                    .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int count = (int) bookings.stream()
                    .filter(b -> b.getBookingDate() != null && !b.getBookingDate().isBefore(start) && !b.getBookingDate().isAfter(end))
                    .count();

            BigDecimal profit = rev.multiply(new BigDecimal("0.45")).setScale(2, RoundingMode.HALF_UP);
            trend.add(new RevenueTrendItemDTO(periodKey, rev, rev, rev, profit, count));
        }
        return trend;
    }
}
