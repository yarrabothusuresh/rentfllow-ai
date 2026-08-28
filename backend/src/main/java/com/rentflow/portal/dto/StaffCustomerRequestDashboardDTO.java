package com.rentflow.portal.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StaffCustomerRequestDashboardDTO {

    public static class RequestItemDTO {
        private UUID id;
        private String requestNumber;
        private String customerName;
        private String companyName;
        private String eventName;
        private String type; // QUOTE_REQUEST, QUESTION, APPROVAL_PENDING, PAYMENT_PENDING, DAMAGE_CLAIM
        private LocalDateTime createdAt;
        private String status;

        public RequestItemDTO() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getRequestNumber() { return requestNumber; }
        public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getEventName() { return eventName; }
        public void setEventName(String eventName) { this.eventName = eventName; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    private long newQuoteRequestsCount;
    private long pendingCustomerQuestionsCount;
    private long quotesAwaitingApprovalCount;
    private long upcomingBookingsCount;
    private long paymentPendingCount;
    private long openDamageClaimsCount;

    private List<RequestItemDTO> requests = new ArrayList<>();

    public StaffCustomerRequestDashboardDTO() {}

    public long getNewQuoteRequestsCount() { return newQuoteRequestsCount; }
    public void setNewQuoteRequestsCount(long newQuoteRequestsCount) { this.newQuoteRequestsCount = newQuoteRequestsCount; }

    public long getPendingCustomerQuestionsCount() { return pendingCustomerQuestionsCount; }
    public void setPendingCustomerQuestionsCount(long pendingCustomerQuestionsCount) { this.pendingCustomerQuestionsCount = pendingCustomerQuestionsCount; }

    public long getQuotesAwaitingApprovalCount() { return quotesAwaitingApprovalCount; }
    public void setQuotesAwaitingApprovalCount(long quotesAwaitingApprovalCount) { this.quotesAwaitingApprovalCount = quotesAwaitingApprovalCount; }

    public long getUpcomingBookingsCount() { return upcomingBookingsCount; }
    public void setUpcomingBookingsCount(long upcomingBookingsCount) { this.upcomingBookingsCount = upcomingBookingsCount; }

    public long getPaymentPendingCount() { return paymentPendingCount; }
    public void setPaymentPendingCount(long paymentPendingCount) { this.paymentPendingCount = paymentPendingCount; }

    public long getOpenDamageClaimsCount() { return openDamageClaimsCount; }
    public void setOpenDamageClaimsCount(long openDamageClaimsCount) { this.openDamageClaimsCount = openDamageClaimsCount; }

    public List<RequestItemDTO> getRequests() { return requests; }
    public void setRequests(List<RequestItemDTO> requests) { this.requests = requests; }
}
