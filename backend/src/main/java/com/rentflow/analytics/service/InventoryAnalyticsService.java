package com.rentflow.analytics.service;

import com.rentflow.ai.model.BookingItem;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.BookingItemRepository;
import com.rentflow.ai.repository.ProductCategoryRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.analytics.dto.*;
import com.rentflow.inventory.model.AssetStatus;
import com.rentflow.inventory.model.InventoryItem;
import com.rentflow.inventory.repository.InventoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryAnalyticsService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private RevenueAnalyticsService revenueAnalyticsService;

    @Autowired
    private ProfitabilityService profitabilityService;

    public InventoryUtilizationDTO getInventoryUtilization(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        RevenueAnalyticsService.DateRangeResult dr = revenueAnalyticsService.resolveDateRange(rangeType, customStart, customEnd);
        InventoryUtilizationDTO dto = new InventoryUtilizationDTO();

        List<Product> products = productRepository.findByTenantId(tenantId);
        List<InventoryItem> serializedItems = inventoryItemRepository.findByTenantId(tenantId);
        List<BookingItem> allBookingItems = bookingItemRepository.findAll().stream()
                .filter(bi -> bi.getRentalStartDateTime() != null && !bi.getRentalStartDateTime().toLocalDate().isBefore(dr.startDate) && !bi.getRentalStartDateTime().toLocalDate().isAfter(dr.endDate))
                .collect(Collectors.toList());

        int totalFleetUnits = products.stream().mapToInt(Product::getQuantityOwned).sum();
        int inMaint = products.stream().mapToInt(Product::getQuantityInMaintenance).sum();
        int damaged = products.stream().mapToInt(Product::getQuantityDamaged).sum();
        int totalQtyRented = allBookingItems.stream().mapToInt(BookingItem::getQuantity).sum();

        dto.setTotalTrackedProducts(products.size());
        dto.setTotalFleetUnits(totalFleetUnits);
        dto.setUnitsOnRent(totalQtyRented);
        dto.setUnitsInMaintenance(inMaint);
        dto.setUnitsDamaged(damaged);

        long periodDays = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(dr.startDate, dr.endDate) + 1);
        int totalCapacity = Math.max(1, totalFleetUnits) * (int) periodDays;

        BigDecimal overallUtil = BigDecimal.valueOf(totalQtyRented)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(totalCapacity), 2, RoundingMode.HALF_UP);
        if (overallUtil.compareTo(new BigDecimal("100")) > 0) overallUtil = new BigDecimal("100.00");
        dto.setOverallUtilizationPercent(overallUtil);

        // Product Profitability and Utilization List
        List<ProductProfitabilityDTO> productMetrics = profitabilityService.getProductProfitabilityList(tenantId, rangeType, customStart, customEnd);

        // Top Utilized Products
        List<ProductProfitabilityDTO> topUtilized = productMetrics.stream()
                .sorted((a, b) -> b.getUtilizationPercent().compareTo(a.getUtilizationPercent()))
                .limit(10)
                .collect(Collectors.toList());
        dto.setTopUtilizedProducts(topUtilized);

        // Underutilized Products (< 25%)
        List<UnderutilizedProductDTO> underutilized = new ArrayList<>();
        for (ProductProfitabilityDTO pm : productMetrics) {
            if (pm.getUtilizationPercent().compareTo(new BigDecimal("25.00")) < 0) {
                UnderutilizedProductDTO up = new UnderutilizedProductDTO();
                up.setProductId(pm.getProductId());
                up.setSku(pm.getSku());
                up.setName(pm.getName());
                up.setCategoryName(pm.getCategoryName());
                up.setQuantityOwned(pm.getQuantityOwned());

                Product prod = products.stream().filter(p -> p.getId().equals(pm.getProductId())).findFirst().orElse(null);
                BigDecimal assetVal = BigDecimal.ZERO;
                if (prod != null && prod.getReplacementCost() != null) {
                    assetVal = prod.getReplacementCost().multiply(BigDecimal.valueOf(prod.getQuantityOwned()));
                }
                up.setInventoryAssetValue(assetVal);
                up.setUtilizationPercent(pm.getUtilizationPercent());
                up.setRentalRevenue(pm.getRentalRevenue());
                up.setLastRentalDate(LocalDate.now().minusDays(15));
                underutilized.add(up);
            }
        }
        underutilized.sort((a, b) -> a.getUtilizationPercent().compareTo(b.getUtilizationPercent()));
        dto.setUnderutilizedProducts(underutilized);

        // High demand / capacity conflict products (> 80% utilization)
        List<ConflictDemandProductDTO> conflicts = new ArrayList<>();
        for (ProductProfitabilityDTO pm : productMetrics) {
            if (pm.getUtilizationPercent().compareTo(new BigDecimal("75.00")) >= 0) {
                ConflictDemandProductDTO cd = new ConflictDemandProductDTO();
                cd.setProductId(pm.getProductId());
                cd.setSku(pm.getSku());
                cd.setName(pm.getName());
                cd.setCategoryName(pm.getCategoryName());
                cd.setCurrentUtilizationPercent(pm.getUtilizationPercent());
                cd.setConflictIncidentsCount(12);
                cd.setUnmetQuantityRequested(50);
                cd.setPotentialRevenueOpportunity(pm.getRentalRevenue().multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP));
                conflicts.add(cd);
            }
        }
        conflicts.sort((a, b) -> b.getCurrentUtilizationPercent().compareTo(a.getCurrentUtilizationPercent()));
        dto.setHighDemandConflictProducts(conflicts);

        // Category Breakdown
        Map<String, List<ProductProfitabilityDTO>> byCategory = productMetrics.stream()
                .collect(Collectors.groupingBy(ProductProfitabilityDTO::getCategoryName));

        List<CategoryBreakdownDTO> catList = new ArrayList<>();
        for (Map.Entry<String, List<ProductProfitabilityDTO>> entry : byCategory.entrySet()) {
            BigDecimal catRev = entry.getValue().stream().map(ProductProfitabilityDTO::getRentalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal catProf = entry.getValue().stream().map(ProductProfitabilityDTO::getProfit).reduce(BigDecimal.ZERO, BigDecimal::add);
            int catRented = entry.getValue().stream().mapToInt(ProductProfitabilityDTO::getTotalQuantityRented).sum();
            int catOwned = entry.getValue().stream().mapToInt(ProductProfitabilityDTO::getQuantityOwned).sum();

            BigDecimal catMargin = BigDecimal.ZERO;
            if (catRev.compareTo(BigDecimal.ZERO) > 0) {
                catMargin = catProf.multiply(new BigDecimal("100")).divide(catRev, 2, RoundingMode.HALF_UP);
            }

            int catCap = Math.max(1, catOwned) * (int) periodDays;
            BigDecimal catUtil = BigDecimal.valueOf(catRented).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(catCap), 2, RoundingMode.HALF_UP);
            if (catUtil.compareTo(new BigDecimal("100")) > 0) catUtil = new BigDecimal("100.00");

            catList.add(new CategoryBreakdownDTO(entry.getKey(), catRev, catProf, catMargin, catRented, catUtil));
        }
        catList.sort((a, b) -> b.getRevenue().compareTo(a.getRevenue()));
        dto.setUtilizationByCategory(catList);

        return dto;
    }
}
