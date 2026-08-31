package com.rentflow.analytics.service;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.analytics.dto.*;
import com.rentflow.claims.model.DamageClaim;
import com.rentflow.claims.model.RepairOrder;
import com.rentflow.claims.repository.DamageClaimRepository;
import com.rentflow.claims.repository.RepairOrderRepository;
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
public class ProfitabilityService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private DamageClaimRepository damageClaimRepository;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Autowired
    private RevenueAnalyticsService revenueAnalyticsService;

    public List<BookingProfitabilityDTO> getBookingProfitabilityList(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        RevenueAnalyticsService.DateRangeResult dr = revenueAnalyticsService.resolveDateRange(rangeType, customStart, customEnd);
        List<Booking> bookings = bookingRepository.findByTenantId(tenantId).stream()
                .filter(b -> b.getBookingDate() != null && !b.getBookingDate().isBefore(dr.startDate) && !b.getBookingDate().isAfter(dr.endDate))
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        List<BookingProfitabilityDTO> result = new ArrayList<>();
        for (Booking b : bookings) {
            result.add(computeBookingProfitabilitySummary(b));
        }

        // Sort by booking date descending
        result.sort((a, b) -> {
            if (a.getBookingDate() == null || b.getBookingDate() == null) return 0;
            return b.getBookingDate().compareTo(a.getBookingDate());
        });

        return result;
    }

    public BookingProfitDetailDTO getBookingProfitDetail(String tenantId, UUID bookingId) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (!tenantId.equals(b.getTenantId())) {
            throw new IllegalArgumentException("Unauthorized tenant access");
        }

        BookingProfitDetailDTO dto = new BookingProfitDetailDTO();
        dto.setBookingId(b.getId());
        dto.setBookingNumber(b.getBookingNumber());
        dto.setStatus(b.getStatus().name());
        dto.setBookingDate(b.getBookingDate());
        dto.setRentalStart(b.getRentalStartDateTime());
        dto.setRentalEnd(b.getRentalEndDateTime());

        // Resolve customer & event names
        if (b.getCustomerId() != null) {
            customerRepository.findById(b.getCustomerId()).ifPresent(c -> {
                dto.setCustomerName(c.getCompanyName() != null && !c.getCompanyName().isBlank() ? c.getCompanyName() : (c.getFirstName() + " " + c.getLastName()));
            });
        }
        if (b.getEventId() != null) {
            eventRepository.findById(b.getEventId()).ifPresent(e -> {
                dto.setEventName(e.getEventName());
            });
        }

        // Revenue streams
        BigDecimal rentRev = b.getSubtotal() != null ? b.getSubtotal() : BigDecimal.ZERO;
        BigDecimal delRev = b.getDeliveryFee() != null ? b.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal setRev = b.getSetupFee() != null ? b.getSetupFee() : BigDecimal.ZERO;
        BigDecimal brkRev = b.getBreakdownFee() != null ? b.getBreakdownFee() : BigDecimal.ZERO;
        BigDecimal svcRev = b.getServiceFee() != null ? b.getServiceFee() : BigDecimal.ZERO;
        BigDecimal dscAmt = b.getDiscountAmount() != null ? b.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal taxAmt = b.getTaxAmount() != null ? b.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal totRev = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;

        dto.setRentalRevenue(rentRev);
        dto.setDeliveryRevenue(delRev);
        dto.setSetupRevenue(setRev);
        dto.setBreakdownRevenue(brkRev);
        dto.setServiceRevenue(svcRev);
        dto.setDiscountAmount(dscAmt);
        dto.setTaxAmount(taxAmt);
        dto.setTotalRevenue(totRev);

        // Direct Cost computation
        List<BookingItem> items = bookingItemRepository.findByBookingId(b.getId());
        BigDecimal invCost = BigDecimal.ZERO;
        for (BookingItem item : items) {
            BigDecimal itemCost = item.getLineSubtotal() != null ? item.getLineSubtotal().multiply(new BigDecimal("0.28")) : BigDecimal.ZERO;
            invCost = invCost.add(itemCost);
        }
        invCost = invCost.setScale(2, RoundingMode.HALF_UP);
        dto.setInventoryAllocationCost(invCost);

        BigDecimal delCost = delRev.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("85.00") : BigDecimal.ZERO;
        BigDecimal pckCost = (b.getPickupFee() != null && b.getPickupFee().compareTo(BigDecimal.ZERO) > 0) ? new BigDecimal("65.00") : BigDecimal.ZERO;
        BigDecimal labCost = (setRev.compareTo(BigDecimal.ZERO) > 0 || brkRev.compareTo(BigDecimal.ZERO) > 0) ? new BigDecimal("120.00") : BigDecimal.ZERO;

        // Attributable damage & repair cost
        BigDecimal dmgCost = BigDecimal.ZERO;
        List<DamageClaim> claims = damageClaimRepository.findByTenantIdAndBookingId(tenantId, b.getId());
        for (DamageClaim dc : claims) {
            if (dc.getFinalTotalCost() != null && dc.getFinalTotalCost().compareTo(BigDecimal.ZERO) > 0) {
                dmgCost = dmgCost.add(dc.getFinalTotalCost());
            } else if (dc.getEstimatedTotalCost() != null) {
                dmgCost = dmgCost.add(dc.getEstimatedTotalCost());
            }
        }
        dto.setDeliveryVehicleCost(delCost);
        dto.setPickupVehicleCost(pckCost);
        dto.setLaborCost(labCost);
        dto.setRepairAndDamageCost(dmgCost);
        dto.setOtherDirectCost(BigDecimal.ZERO);

        BigDecimal totalCost = invCost.add(delCost).add(pckCost).add(labCost).add(dmgCost);
        dto.setTotalDirectCost(totalCost);

        BigDecimal profit = totRev.subtract(totalCost);
        dto.setGrossProfit(profit);

        BigDecimal marginPct = BigDecimal.ZERO;
        if (totRev.compareTo(BigDecimal.ZERO) > 0) {
            marginPct = profit.multiply(new BigDecimal("100")).divide(totRev, 2, RoundingMode.HALF_UP);
        }
        dto.setGrossMarginPercent(marginPct);
        dto.setMarginFlag(determineMarginFlag(marginPct, profit));

        // Cost Source explanations
        dto.getCostSources().add(new CostSourceExplanationDTO("Inventory Direct Amortization", invCost, "28% of rental catalog depreciation model", "ESTIMATED"));
        if (delCost.compareTo(BigDecimal.ZERO) > 0) {
            dto.getCostSources().add(new CostSourceExplanationDTO("Delivery Fleet Vehicle", delCost, "Standard transit route allocation ($85.00/trip)", "ESTIMATED"));
        }
        if (labCost.compareTo(BigDecimal.ZERO) > 0) {
            dto.getCostSources().add(new CostSourceExplanationDTO("On-Site Staging & Setup Labor", labCost, "Field operator staging schedule allocation ($120.00)", "ESTIMATED"));
        }
        if (dmgCost.compareTo(BigDecimal.ZERO) > 0) {
            dto.getCostSources().add(new CostSourceExplanationDTO("Damage Claims & Repairs", dmgCost, "Attributable claims registered on return check-in", "ACTUAL"));
        }

        return dto;
    }

    public List<ProductProfitabilityDTO> getProductProfitabilityList(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        RevenueAnalyticsService.DateRangeResult dr = revenueAnalyticsService.resolveDateRange(rangeType, customStart, customEnd);
        List<Product> products = productRepository.findByTenantId(tenantId);
        List<BookingItem> bookingItems = bookingItemRepository.findAll().stream()
                .filter(bi -> bi.getRentalStartDateTime() != null && !bi.getRentalStartDateTime().toLocalDate().isBefore(dr.startDate) && !bi.getRentalStartDateTime().toLocalDate().isAfter(dr.endDate))
                .collect(Collectors.toList());

        List<ProductProfitabilityDTO> list = new ArrayList<>();
        for (Product p : products) {
            ProductProfitabilityDTO dto = new ProductProfitabilityDTO();
            dto.setProductId(p.getId());
            dto.setSku(p.getSku());
            dto.setName(p.getName());
            dto.setQuantityOwned(p.getQuantityOwned());

            if (p.getCategoryId() != null) {
                categoryRepository.findById(p.getCategoryId()).ifPresent(cat -> dto.setCategoryName(cat.getName()));
            }
            if (dto.getCategoryName() == null) dto.setCategoryName("General Rental");

            List<BookingItem> matchingItems = bookingItems.stream()
                    .filter(bi -> p.getId().equals(bi.getProductId()))
                    .collect(Collectors.toList());

            BigDecimal rev = matchingItems.stream()
                    .map(bi -> bi.getLineSubtotal() != null ? bi.getLineSubtotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int qtyRented = matchingItems.stream().mapToInt(BookingItem::getQuantity).sum();

            dto.setRentalRevenue(rev);
            dto.setRentalCount(matchingItems.size());
            dto.setTotalQuantityRented(qtyRented);

            // Direct Cost Amortization (approx 25% of rental price)
            BigDecimal directCost = rev.multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP);
            dto.setDirectCost(directCost);

            // Repair & Damage costs
            BigDecimal repairCost = BigDecimal.ZERO;
            List<RepairOrder> repairs = repairOrderRepository.findByTenantIdAndProductId(tenantId, p.getId());
            for (RepairOrder ro : repairs) {
                if (ro.getActualCost() != null && ro.getActualCost().compareTo(BigDecimal.ZERO) > 0) {
                    repairCost = repairCost.add(ro.getActualCost());
                }
            }
            dto.setRepairCost(repairCost);
            dto.setDamageCost(BigDecimal.ZERO);
            dto.setReplacementLossCost(BigDecimal.ZERO);

            BigDecimal profit = rev.subtract(directCost).subtract(repairCost);
            dto.setProfit(profit);

            BigDecimal margin = BigDecimal.ZERO;
            if (rev.compareTo(BigDecimal.ZERO) > 0) {
                margin = profit.multiply(new BigDecimal("100")).divide(rev, 2, RoundingMode.HALF_UP);
            }
            dto.setMarginPercent(margin);
            dto.setMarginFlag(determineMarginFlag(margin, profit));

            // Utilization %
            long periodDays = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(dr.startDate, dr.endDate) + 1);
            int capacity = Math.max(1, p.getQuantityOwned()) * (int) periodDays;
            BigDecimal util = BigDecimal.valueOf(qtyRented).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(capacity), 2, RoundingMode.HALF_UP);
            if (util.compareTo(new BigDecimal("100")) > 0) util = new BigDecimal("100.00");
            dto.setUtilizationPercent(util);

            list.add(dto);
        }

        list.sort((a, b) -> b.getRentalRevenue().compareTo(a.getRentalRevenue()));
        return list;
    }

    public ProductProfitDetailDTO getProductProfitDetail(String tenantId, UUID productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (!tenantId.equals(p.getTenantId())) {
            throw new IllegalArgumentException("Unauthorized tenant access");
        }

        ProductProfitDetailDTO dto = new ProductProfitDetailDTO();
        dto.setProductId(p.getId());
        dto.setSku(p.getSku());
        dto.setName(p.getName());
        dto.setTrackingType(p.getTrackingType() != null ? p.getTrackingType().name() : "QUANTITY");
        dto.setQuantityOwned(p.getQuantityOwned());
        dto.setQuantityInMaintenance(p.getQuantityInMaintenance());
        dto.setQuantityDamaged(p.getQuantityDamaged());
        dto.setQuantityLost(p.getQuantityLost());
        dto.setRentalPrice(p.getRentalPrice() != null ? p.getRentalPrice() : BigDecimal.ZERO);
        dto.setReplacementCost(p.getReplacementCost() != null ? p.getReplacementCost() : BigDecimal.ZERO);

        if (p.getCategoryId() != null) {
            categoryRepository.findById(p.getCategoryId()).ifPresent(cat -> dto.setCategoryName(cat.getName()));
        }
        if (dto.getCategoryName() == null) dto.setCategoryName("General");

        List<BookingItem> items = bookingItemRepository.findByProductId(p.getId());
        BigDecimal totalRev = items.stream()
                .map(bi -> bi.getLineSubtotal() != null ? bi.getLineSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalQty = items.stream().mapToInt(BookingItem::getQuantity).sum();

        dto.setRentalRevenue(totalRev);
        dto.setBookingCount(items.size());
        dto.setTotalQuantityRented(totalQty);
        dto.setAverageRentalPrice(p.getRentalPrice());

        BigDecimal directCost = totalRev.multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP);
        dto.setDirectProductCost(directCost);

        BigDecimal repairCost = BigDecimal.ZERO;
        List<RepairOrder> repairs = repairOrderRepository.findByTenantIdAndProductId(tenantId, p.getId());
        for (RepairOrder ro : repairs) {
            if (ro.getActualCost() != null && ro.getActualCost().compareTo(BigDecimal.ZERO) > 0) {
                repairCost = repairCost.add(ro.getActualCost());
            }
        }
        dto.setRepairCost(repairCost);
        dto.setDamageCost(BigDecimal.ZERO);
        dto.setReplacementLossCost(BigDecimal.ZERO);

        BigDecimal totalCost = directCost.add(repairCost);
        dto.setTotalCost(totalCost);

        BigDecimal profit = totalRev.subtract(totalCost);
        dto.setProfit(profit);

        BigDecimal margin = BigDecimal.ZERO;
        if (totalRev.compareTo(BigDecimal.ZERO) > 0) {
            margin = profit.multiply(new BigDecimal("100")).divide(totalRev, 2, RoundingMode.HALF_UP);
        }
        dto.setMarginPercent(margin);
        dto.setMarginFlag(determineMarginFlag(margin, profit));

        int capacity = Math.max(1, p.getQuantityOwned()) * 180; // 6 month capacity
        BigDecimal util = BigDecimal.valueOf(totalQty).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(capacity), 2, RoundingMode.HALF_UP);
        if (util.compareTo(new BigDecimal("100")) > 0) util = new BigDecimal("100.00");
        dto.setUtilizationPercent(util);

        // Monthly Trend
        dto.setMonthlyTrend(generateProductMonthlyTrend(items));

        dto.getCostSources().add(new CostSourceExplanationDTO("Direct Asset Amortization", directCost, "25% Standard Equipment Life Cycle Depreciation", "ESTIMATED"));
        if (repairCost.compareTo(BigDecimal.ZERO) > 0) {
            dto.getCostSources().add(new CostSourceExplanationDTO("Repair & Maintenance Work Orders", repairCost, "Actual maintenance cost from closed Repair Orders", "ACTUAL"));
        }

        return dto;
    }

    private List<RevenueTrendItemDTO> generateProductMonthlyTrend(List<BookingItem> items) {
        LocalDate today = LocalDate.now();
        List<RevenueTrendItemDTO> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 5; i >= 0; i--) {
            LocalDate monthTarget = today.minusMonths(i);
            LocalDate start = monthTarget.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate end = monthTarget.with(TemporalAdjusters.lastDayOfMonth());
            String periodKey = monthTarget.format(fmt);

            BigDecimal rev = items.stream()
                    .filter(bi -> bi.getRentalStartDateTime() != null && !bi.getRentalStartDateTime().toLocalDate().isBefore(start) && !bi.getRentalStartDateTime().toLocalDate().isAfter(end))
                    .map(bi -> bi.getLineSubtotal() != null ? bi.getLineSubtotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int count = (int) items.stream()
                    .filter(bi -> bi.getRentalStartDateTime() != null && !bi.getRentalStartDateTime().toLocalDate().isBefore(start) && !bi.getRentalStartDateTime().toLocalDate().isAfter(end))
                    .count();

            BigDecimal profit = rev.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
            trend.add(new RevenueTrendItemDTO(periodKey, rev, rev, rev, profit, count));
        }
        return trend;
    }

    private BookingProfitabilityDTO computeBookingProfitabilitySummary(Booking b) {
        BookingProfitabilityDTO dto = new BookingProfitabilityDTO();
        dto.setBookingId(b.getId());
        dto.setBookingNumber(b.getBookingNumber());
        dto.setStatus(b.getStatus().name());
        dto.setBookingDate(b.getBookingDate());

        if (b.getCustomerId() != null) {
            customerRepository.findById(b.getCustomerId()).ifPresent(c -> {
                dto.setCustomerName(c.getCompanyName() != null && !c.getCompanyName().isBlank() ? c.getCompanyName() : (c.getFirstName() + " " + c.getLastName()));
            });
        }
        if (b.getEventId() != null) {
            eventRepository.findById(b.getEventId()).ifPresent(e -> dto.setEventName(e.getEventName()));
        }

        BigDecimal totRev = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;
        dto.setTotalRevenue(totRev);

        // Approximate direct costs for summary table
        BigDecimal dirCost = (b.getSubtotal() != null ? b.getSubtotal().multiply(new BigDecimal("0.35")) : BigDecimal.ZERO)
                .add(b.getDeliveryFee() != null && b.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("85.00") : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        dto.setDirectCost(dirCost);

        BigDecimal profit = totRev.subtract(dirCost);
        dto.setGrossProfit(profit);

        BigDecimal margin = BigDecimal.ZERO;
        if (totRev.compareTo(BigDecimal.ZERO) > 0) {
            margin = profit.multiply(new BigDecimal("100")).divide(totRev, 2, RoundingMode.HALF_UP);
        }
        dto.setGrossMarginPercent(margin);
        dto.setMarginFlag(determineMarginFlag(margin, profit));
        dto.setWarningFlag("NEGATIVE_MARGIN".equals(dto.getMarginFlag()) || "LOW_MARGIN".equals(dto.getMarginFlag()));

        return dto;
    }

    public String determineMarginFlag(BigDecimal marginPercent, BigDecimal profit) {
        if (profit.compareTo(BigDecimal.ZERO) < 0 || marginPercent.compareTo(BigDecimal.ZERO) < 0) {
            return "NEGATIVE_MARGIN";
        }
        if (marginPercent.compareTo(new BigDecimal("50.00")) >= 0) {
            return "HIGH_MARGIN";
        }
        if (marginPercent.compareTo(new BigDecimal("30.00")) >= 0) {
            return "HEALTHY_MARGIN";
        }
        return "LOW_MARGIN";
    }
}
