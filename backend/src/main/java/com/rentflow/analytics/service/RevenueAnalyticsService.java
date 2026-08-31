package com.rentflow.analytics.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Event;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.ProductCategoryRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.analytics.dto.*;
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
public class RevenueAnalyticsService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    public DateRangeResult resolveDateRange(DateRangeType dateRangeType, LocalDate customStart, LocalDate customEnd) {
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end;
        LocalDate prevStart;
        LocalDate prevEnd;
        String name;

        if (dateRangeType == null) {
            dateRangeType = DateRangeType.THIS_MONTH;
        }

        switch (dateRangeType) {
            case TODAY:
                start = today;
                end = today;
                prevStart = today.minusDays(1);
                prevEnd = today.minusDays(1);
                name = "Today";
                break;
            case YESTERDAY:
                start = today.minusDays(1);
                end = today.minusDays(1);
                prevStart = today.minusDays(2);
                prevEnd = today.minusDays(2);
                name = "Yesterday";
                break;
            case THIS_WEEK:
                start = today.with(java.time.DayOfWeek.MONDAY);
                end = today.with(java.time.DayOfWeek.SUNDAY);
                prevStart = start.minusWeeks(1);
                prevEnd = end.minusWeeks(1);
                name = "This Week";
                break;
            case LAST_WEEK:
                start = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
                end = today.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY);
                prevStart = start.minusWeeks(1);
                prevEnd = end.minusWeeks(1);
                name = "Last Week";
                break;
            case LAST_MONTH:
                LocalDate lastMonth = today.minusMonths(1);
                start = lastMonth.with(TemporalAdjusters.firstDayOfMonth());
                end = lastMonth.with(TemporalAdjusters.lastDayOfMonth());
                LocalDate twoMonthsAgo = today.minusMonths(2);
                prevStart = twoMonthsAgo.with(TemporalAdjusters.firstDayOfMonth());
                prevEnd = twoMonthsAgo.with(TemporalAdjusters.lastDayOfMonth());
                name = "Last Month";
                break;
            case THIS_QUARTER:
                int currentQuarterMonth = ((today.getMonthValue() - 1) / 3) * 3 + 1;
                start = LocalDate.of(today.getYear(), currentQuarterMonth, 1);
                end = start.plusMonths(3).minusDays(1);
                prevStart = start.minusMonths(3);
                prevEnd = start.minusDays(1);
                name = "This Quarter";
                break;
            case THIS_YEAR:
                start = LocalDate.of(today.getYear(), 1, 1);
                end = LocalDate.of(today.getYear(), 12, 31);
                prevStart = LocalDate.of(today.getYear() - 1, 1, 1);
                prevEnd = LocalDate.of(today.getYear() - 1, 12, 31);
                name = "This Year";
                break;
            case LAST_YEAR:
                start = LocalDate.of(today.getYear() - 1, 1, 1);
                end = LocalDate.of(today.getYear() - 1, 12, 31);
                prevStart = LocalDate.of(today.getYear() - 2, 1, 1);
                prevEnd = LocalDate.of(today.getYear() - 2, 12, 31);
                name = "Last Year";
                break;
            case CUSTOM:
                start = customStart != null ? customStart : today.with(TemporalAdjusters.firstDayOfMonth());
                end = customEnd != null ? customEnd : today;
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                prevStart = start.minusDays(days);
                prevEnd = start.minusDays(1);
                name = "Custom Range (" + start + " to " + end + ")";
                break;
            case THIS_MONTH:
            default:
                start = today.with(TemporalAdjusters.firstDayOfMonth());
                end = today.with(TemporalAdjusters.lastDayOfMonth());
                LocalDate prevMonth = today.minusMonths(1);
                prevStart = prevMonth.with(TemporalAdjusters.firstDayOfMonth());
                prevEnd = prevMonth.with(TemporalAdjusters.lastDayOfMonth());
                name = "This Month";
                break;
        }

        return new DateRangeResult(name, start, end, prevStart, prevEnd);
    }

    public RevenueAnalyticsDTO getRevenueAnalytics(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        DateRangeResult dr = resolveDateRange(rangeType, customStart, customEnd);
        RevenueAnalyticsDTO dto = new RevenueAnalyticsDTO();
        dto.setCurrency("USD");

        List<Booking> allBookings = bookingRepository.findByTenantId(tenantId);
        List<Invoice> allInvoices = invoiceRepository.findByTenantId(tenantId);
        List<Payment> allPayments = paymentRepository.findByTenantId(tenantId);

        // Filter current period
        List<Booking> currentBookings = allBookings.stream()
                .filter(b -> b.getBookingDate() != null && !b.getBookingDate().isBefore(dr.startDate) && !b.getBookingDate().isAfter(dr.endDate))
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        List<Booking> prevBookings = allBookings.stream()
                .filter(b -> b.getBookingDate() != null && !b.getBookingDate().isBefore(dr.prevStartDate) && !b.getBookingDate().isAfter(dr.prevEndDate))
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        // Booked Revenue
        BigDecimal bookedRev = currentBookings.stream()
                .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalBookedRevenue(bookedRev);

        BigDecimal prevBookedRev = prevBookings.stream()
                .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (prevBookedRev.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = bookedRev.subtract(prevBookedRev);
            dto.setRevenueGrowthPercent(diff.multiply(new BigDecimal("100")).divide(prevBookedRev, 2, RoundingMode.HALF_UP));
        } else if (bookedRev.compareTo(BigDecimal.ZERO) > 0) {
            dto.setRevenueGrowthPercent(new BigDecimal("100.00"));
        } else {
            dto.setRevenueGrowthPercent(BigDecimal.ZERO);
        }

        // Invoiced Revenue
        BigDecimal invoicedRev = allInvoices.stream()
                .filter(i -> i.getIssueDate() != null && !i.getIssueDate().isBefore(dr.startDate) && !i.getIssueDate().isAfter(dr.endDate))
                .filter(i -> i.getStatus() != InvoiceStatus.VOID)
                .map(i -> i.getTotalAmount() != null ? i.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalInvoicedRevenue(invoicedRev);

        // Collected Revenue
        BigDecimal collectedRev = allPayments.stream()
                .filter(p -> p.getPaymentDate() != null && !p.getPaymentDate().isBefore(dr.startDate) && !p.getPaymentDate().isAfter(dr.endDate))
                .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalCollectedRevenue(collectedRev);

        // Outstanding Revenue
        BigDecimal outstanding = allInvoices.stream()
                .filter(i -> i.getStatus() != InvoiceStatus.VOID && i.getStatus() != InvoiceStatus.PAID)
                .map(i -> i.getBalanceDue() != null ? i.getBalanceDue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalOutstandingRevenue(outstanding);

        // Average Booking Value
        if (!currentBookings.isEmpty()) {
            dto.setAverageBookingValue(bookedRev.divide(BigDecimal.valueOf(currentBookings.size()), 2, RoundingMode.HALF_UP));
        }

        // Monthly Trend
        dto.setRevenueTrend(generateRevenueTrend(tenantId, allBookings, allInvoices, allPayments));

        // Revenue by Event Type
        Map<String, BigDecimal> eventTypeMap = new HashMap<>();
        Map<String, Integer> eventCountMap = new HashMap<>();
        for (Booking b : currentBookings) {
            String typeName = "Standard Event";
            if (b.getEventId() != null) {
                Optional<Event> evOpt = eventRepository.findById(b.getEventId());
                if (evOpt.isPresent() && evOpt.get().getEventType() != null) {
                    typeName = evOpt.get().getEventType().name();
                }
            }
            BigDecimal amt = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;
            eventTypeMap.put(typeName, eventTypeMap.getOrDefault(typeName, BigDecimal.ZERO).add(amt));
            eventCountMap.put(typeName, eventCountMap.getOrDefault(typeName, 0) + 1);
        }

        List<NamedMetricDTO> eventTypeList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : eventTypeMap.entrySet()) {
            BigDecimal pct = bookedRev.compareTo(BigDecimal.ZERO) > 0 ? entry.getValue().multiply(new BigDecimal("100")).divide(bookedRev, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            eventTypeList.add(new NamedMetricDTO(entry.getKey(), entry.getValue(), eventCountMap.getOrDefault(entry.getKey(), 0), pct));
        }
        eventTypeList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        dto.setRevenueByEventType(eventTypeList);

        // Revenue by Booking Status
        Map<String, BigDecimal> statusMap = new HashMap<>();
        Map<String, Integer> statusCountMap = new HashMap<>();
        for (Booking b : currentBookings) {
            String s = b.getStatus().name();
            BigDecimal amt = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;
            statusMap.put(s, statusMap.getOrDefault(s, BigDecimal.ZERO).add(amt));
            statusCountMap.put(s, statusCountMap.getOrDefault(s, 0) + 1);
        }
        List<NamedMetricDTO> statusList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : statusMap.entrySet()) {
            BigDecimal pct = bookedRev.compareTo(BigDecimal.ZERO) > 0 ? entry.getValue().multiply(new BigDecimal("100")).divide(bookedRev, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            statusList.add(new NamedMetricDTO(entry.getKey(), entry.getValue(), statusCountMap.getOrDefault(entry.getKey(), 0), pct));
        }
        dto.setRevenueByBookingStatus(statusList);

        return dto;
    }

    public List<RevenueTrendItemDTO> generateRevenueTrend(String tenantId, List<Booking> bookings, List<Invoice> invoices, List<Payment> payments) {
        LocalDate today = LocalDate.now();
        List<RevenueTrendItemDTO> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 5; i >= 0; i--) {
            LocalDate monthTarget = today.minusMonths(i);
            LocalDate start = monthTarget.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate end = monthTarget.with(TemporalAdjusters.lastDayOfMonth());
            String periodKey = monthTarget.format(fmt);

            BigDecimal bRev = bookings.stream()
                    .filter(b -> b.getBookingDate() != null && !b.getBookingDate().isBefore(start) && !b.getBookingDate().isAfter(end) && b.getStatus() != BookingStatus.CANCELLED)
                    .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int bCount = (int) bookings.stream()
                    .filter(b -> b.getBookingDate() != null && !b.getBookingDate().isBefore(start) && !b.getBookingDate().isAfter(end) && b.getStatus() != BookingStatus.CANCELLED)
                    .count();

            BigDecimal iRev = invoices.stream()
                    .filter(inv -> inv.getIssueDate() != null && !inv.getIssueDate().isBefore(start) && !inv.getIssueDate().isAfter(end) && inv.getStatus() != InvoiceStatus.VOID)
                    .map(inv -> inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal cRev = payments.stream()
                    .filter(p -> p.getPaymentDate() != null && !p.getPaymentDate().isBefore(start) && !p.getPaymentDate().isAfter(end) && p.getPaymentStatus() == PaymentStatus.COMPLETED)
                    .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Estimated gross profit (approx 42% margin baseline)
            BigDecimal estProfit = bRev.multiply(new BigDecimal("0.42")).setScale(2, RoundingMode.HALF_UP);

            trend.add(new RevenueTrendItemDTO(periodKey, bRev, iRev, cRev, estProfit, bCount));
        }

        return trend;
    }

    public static class DateRangeResult {
        public final String name;
        public final LocalDate startDate;
        public final LocalDate endDate;
        public final LocalDate prevStartDate;
        public final LocalDate prevEndDate;

        public DateRangeResult(String name, LocalDate startDate, LocalDate endDate, LocalDate prevStartDate, LocalDate prevEndDate) {
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
            this.prevStartDate = prevStartDate;
            this.prevEndDate = prevEndDate;
        }
    }
}
