package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PaymentAndArAnalyticsDTO {
    private BigDecimal totalCollectedAmount = BigDecimal.ZERO;
    private BigDecimal totalOutstandingAmount = BigDecimal.ZERO;
    private BigDecimal totalOverdueAmount = BigDecimal.ZERO;
    private BigDecimal collectionRatePercent = BigDecimal.ZERO;
    private BigDecimal averageDaysToPay = BigDecimal.ZERO;

    private int completedPaymentsCount = 0;
    private int failedPaymentsCount = 0;

    private List<ArAgingBucketDTO> arAgingBuckets = new ArrayList<>();
    private List<NamedMetricDTO> paymentsByMethod = new ArrayList<>();

    public BigDecimal getTotalCollectedAmount() { return totalCollectedAmount; }
    public void setTotalCollectedAmount(BigDecimal totalCollectedAmount) { this.totalCollectedAmount = totalCollectedAmount; }

    public BigDecimal getTotalOutstandingAmount() { return totalOutstandingAmount; }
    public void setTotalOutstandingAmount(BigDecimal totalOutstandingAmount) { this.totalOutstandingAmount = totalOutstandingAmount; }

    public BigDecimal getTotalOverdueAmount() { return totalOverdueAmount; }
    public void setTotalOverdueAmount(BigDecimal totalOverdueAmount) { this.totalOverdueAmount = totalOverdueAmount; }

    public BigDecimal getCollectionRatePercent() { return collectionRatePercent; }
    public void setCollectionRatePercent(BigDecimal collectionRatePercent) { this.collectionRatePercent = collectionRatePercent; }

    public BigDecimal getAverageDaysToPay() { return averageDaysToPay; }
    public void setAverageDaysToPay(BigDecimal averageDaysToPay) { this.averageDaysToPay = averageDaysToPay; }

    public int getCompletedPaymentsCount() { return completedPaymentsCount; }
    public void setCompletedPaymentsCount(int completedPaymentsCount) { this.completedPaymentsCount = completedPaymentsCount; }

    public int getFailedPaymentsCount() { return failedPaymentsCount; }
    public void setFailedPaymentsCount(int failedPaymentsCount) { this.failedPaymentsCount = failedPaymentsCount; }

    public List<ArAgingBucketDTO> getArAgingBuckets() { return arAgingBuckets; }
    public void setArAgingBuckets(List<ArAgingBucketDTO> arAgingBuckets) { this.arAgingBuckets = arAgingBuckets; }

    public List<NamedMetricDTO> getPaymentsByMethod() { return paymentsByMethod; }
    public void setPaymentsByMethod(List<NamedMetricDTO> paymentsByMethod) { this.paymentsByMethod = paymentsByMethod; }
}
