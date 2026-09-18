package com.rentflow.workflow.dto;

import com.rentflow.workflow.model.WorkflowStage;

import java.math.BigDecimal;
import java.util.List;

public class WorkflowStatusDTO {
    private String bookingId;
    private WorkflowStage currentStage;
    private int progress;
    private List<WorkflowStageDTO> stages;
    private DemoScenarioDTO demoScenario;

    public WorkflowStatusDTO() {}

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public WorkflowStage getCurrentStage() { return currentStage; }
    public void setCurrentStage(WorkflowStage currentStage) { this.currentStage = currentStage; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public List<WorkflowStageDTO> getStages() { return stages; }
    public void setStages(List<WorkflowStageDTO> stages) { this.stages = stages; }

    public DemoScenarioDTO getDemoScenario() { return demoScenario; }
    public void setDemoScenario(DemoScenarioDTO demoScenario) { this.demoScenario = demoScenario; }

    public static class DemoScenarioDTO {
        private String customerName;
        private String customerLocation;
        private String eventType;
        private String eventDate;
        private int guestCount;
        private String companyName;
        private List<String> products;
        private BigDecimal estimatedRental;
        private BigDecimal deliverySetup;
        private BigDecimal totalEstimated;
        private BigDecimal estimatedMargin;
        private BigDecimal estimatedCost;

        public DemoScenarioDTO() {}

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getCustomerLocation() { return customerLocation; }
        public void setCustomerLocation(String customerLocation) { this.customerLocation = customerLocation; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public String getEventDate() { return eventDate; }
        public void setEventDate(String eventDate) { this.eventDate = eventDate; }

        public int getGuestCount() { return guestCount; }
        public void setGuestCount(int guestCount) { this.guestCount = guestCount; }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public List<String> getProducts() { return products; }
        public void setProducts(List<String> products) { this.products = products; }

        public BigDecimal getEstimatedRental() { return estimatedRental; }
        public void setEstimatedRental(BigDecimal estimatedRental) { this.estimatedRental = estimatedRental; }

        public BigDecimal getDeliverySetup() { return deliverySetup; }
        public void setDeliverySetup(BigDecimal deliverySetup) { this.deliverySetup = deliverySetup; }

        public BigDecimal getTotalEstimated() { return totalEstimated; }
        public void setTotalEstimated(BigDecimal totalEstimated) { this.totalEstimated = totalEstimated; }

        public BigDecimal getEstimatedMargin() { return estimatedMargin; }
        public void setEstimatedMargin(BigDecimal estimatedMargin) { this.estimatedMargin = estimatedMargin; }

        public BigDecimal getEstimatedCost() { return estimatedCost; }
        public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    }
}
