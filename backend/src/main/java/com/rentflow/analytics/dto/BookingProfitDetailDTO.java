package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingProfitDetailDTO {
    private UUID bookingId;
    private String bookingNumber;
    private String customerName;
    private String eventName;
    private String status;
    private LocalDate bookingDate;
    private LocalDateTime rentalStart;
    private LocalDateTime rentalEnd;

    // Revenue streams
    private BigDecimal rentalRevenue = BigDecimal.ZERO;
    private BigDecimal deliveryRevenue = BigDecimal.ZERO;
    private BigDecimal setupRevenue = BigDecimal.ZERO;
    private BigDecimal breakdownRevenue = BigDecimal.ZERO;
    private BigDecimal serviceRevenue = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    // Direct Cost breakdown
    private BigDecimal inventoryAllocationCost = BigDecimal.ZERO;
    private BigDecimal deliveryVehicleCost = BigDecimal.ZERO;
    private BigDecimal pickupVehicleCost = BigDecimal.ZERO;
    private BigDecimal laborCost = BigDecimal.ZERO;
    private BigDecimal repairAndDamageCost = BigDecimal.ZERO;
    private BigDecimal otherDirectCost = BigDecimal.ZERO;
    private BigDecimal totalDirectCost = BigDecimal.ZERO;

    // Profit & Margin
    private BigDecimal grossProfit = BigDecimal.ZERO;
    private BigDecimal grossMarginPercent = BigDecimal.ZERO;
    private String marginFlag; // HIGH_MARGIN, HEALTHY_MARGIN, LOW_MARGIN, NEGATIVE_MARGIN

    // Cost source explanation and data coverage
    private List<CostSourceExplanationDTO> costSources = new ArrayList<>();
    private List<String> dataQualityWarnings = new ArrayList<>();

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalDateTime getRentalStart() { return rentalStart; }
    public void setRentalStart(LocalDateTime rentalStart) { this.rentalStart = rentalStart; }

    public LocalDateTime getRentalEnd() { return rentalEnd; }
    public void setRentalEnd(LocalDateTime rentalEnd) { this.rentalEnd = rentalEnd; }

    public BigDecimal getRentalRevenue() { return rentalRevenue; }
    public void setRentalRevenue(BigDecimal rentalRevenue) { this.rentalRevenue = rentalRevenue; }

    public BigDecimal getDeliveryRevenue() { return deliveryRevenue; }
    public void setDeliveryRevenue(BigDecimal deliveryRevenue) { this.deliveryRevenue = deliveryRevenue; }

    public BigDecimal getSetupRevenue() { return setupRevenue; }
    public void setSetupRevenue(BigDecimal setupRevenue) { this.setupRevenue = setupRevenue; }

    public BigDecimal getBreakdownRevenue() { return breakdownRevenue; }
    public void setBreakdownRevenue(BigDecimal breakdownRevenue) { this.breakdownRevenue = breakdownRevenue; }

    public BigDecimal getServiceRevenue() { return serviceRevenue; }
    public void setServiceRevenue(BigDecimal serviceRevenue) { this.serviceRevenue = serviceRevenue; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getInventoryAllocationCost() { return inventoryAllocationCost; }
    public void setInventoryAllocationCost(BigDecimal inventoryAllocationCost) { this.inventoryAllocationCost = inventoryAllocationCost; }

    public BigDecimal getDeliveryVehicleCost() { return deliveryVehicleCost; }
    public void setDeliveryVehicleCost(BigDecimal deliveryVehicleCost) { this.deliveryVehicleCost = deliveryVehicleCost; }

    public BigDecimal getPickupVehicleCost() { return pickupVehicleCost; }
    public void setPickupVehicleCost(BigDecimal pickupVehicleCost) { this.pickupVehicleCost = pickupVehicleCost; }

    public BigDecimal getLaborCost() { return laborCost; }
    public void setLaborCost(BigDecimal laborCost) { this.laborCost = laborCost; }

    public BigDecimal getRepairAndDamageCost() { return repairAndDamageCost; }
    public void setRepairAndDamageCost(BigDecimal repairAndDamageCost) { this.repairAndDamageCost = repairAndDamageCost; }

    public BigDecimal getOtherDirectCost() { return otherDirectCost; }
    public void setOtherDirectCost(BigDecimal otherDirectCost) { this.otherDirectCost = otherDirectCost; }

    public BigDecimal getTotalDirectCost() { return totalDirectCost; }
    public void setTotalDirectCost(BigDecimal totalDirectCost) { this.totalDirectCost = totalDirectCost; }

    public BigDecimal getGrossProfit() { return grossProfit; }
    public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = grossProfit; }

    public BigDecimal getGrossMarginPercent() { return grossMarginPercent; }
    public void setGrossMarginPercent(BigDecimal grossMarginPercent) { this.grossMarginPercent = grossMarginPercent; }

    public String getMarginFlag() { return marginFlag; }
    public void setMarginFlag(String marginFlag) { this.marginFlag = marginFlag; }

    public List<CostSourceExplanationDTO> getCostSources() { return costSources; }
    public void setCostSources(List<CostSourceExplanationDTO> costSources) { this.costSources = costSources; }

    public List<String> getDataQualityWarnings() { return dataQualityWarnings; }
    public void setDataQualityWarnings(List<String> dataQualityWarnings) { this.dataQualityWarnings = dataQualityWarnings; }
}
