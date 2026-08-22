package com.rentflow.portal.dto;

import java.math.BigDecimal;
import java.util.List;

public class CustomerPortalDashboardDTO {
    private String customerName;
    private String companyName;
    private CustomerPortalEventDTO upcomingEvent;
    private long activeQuotesCount;
    private long activeBookingsCount;
    private long invoicesCount;
    private BigDecimal outstandingBalance;
    private List<RecentActivityDTO> recentActivities;

    public CustomerPortalDashboardDTO() {}

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public CustomerPortalEventDTO getUpcomingEvent() { return upcomingEvent; }
    public void setUpcomingEvent(CustomerPortalEventDTO upcomingEvent) { this.upcomingEvent = upcomingEvent; }

    public long getActiveQuotesCount() { return activeQuotesCount; }
    public void setActiveQuotesCount(long activeQuotesCount) { this.activeQuotesCount = activeQuotesCount; }

    public long getActiveBookingsCount() { return activeBookingsCount; }
    public void setActiveBookingsCount(long activeBookingsCount) { this.activeBookingsCount = activeBookingsCount; }

    public long getInvoicesCount() { return invoicesCount; }
    public void setInvoicesCount(long invoicesCount) { this.invoicesCount = invoicesCount; }

    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }

    public List<RecentActivityDTO> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<RecentActivityDTO> recentActivities) { this.recentActivities = recentActivities; }

    public static class RecentActivityDTO {
        private String type;
        private String title;
        private String description;
        private String timestamp;

        public RecentActivityDTO() {}

        public RecentActivityDTO(String type, String title, String description, String timestamp) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
