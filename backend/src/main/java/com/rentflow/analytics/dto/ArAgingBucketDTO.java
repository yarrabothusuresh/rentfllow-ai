package com.rentflow.analytics.dto;

import java.math.BigDecimal;

public class ArAgingBucketDTO {
    private String bucketName; // CURRENT, 1-30_DAYS, 31-60_DAYS, 61-90_DAYS, 90+_DAYS
    private BigDecimal amount = BigDecimal.ZERO;
    private int invoiceCount = 0;
    private BigDecimal percentageOfTotal = BigDecimal.ZERO;

    public ArAgingBucketDTO() {}

    public ArAgingBucketDTO(String bucketName, BigDecimal amount, int invoiceCount, BigDecimal percentageOfTotal) {
        this.bucketName = bucketName;
        this.amount = amount;
        this.invoiceCount = invoiceCount;
        this.percentageOfTotal = percentageOfTotal;
    }

    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getInvoiceCount() { return invoiceCount; }
    public void setInvoiceCount(int invoiceCount) { this.invoiceCount = invoiceCount; }

    public BigDecimal getPercentageOfTotal() { return percentageOfTotal; }
    public void setPercentageOfTotal(BigDecimal percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
}
