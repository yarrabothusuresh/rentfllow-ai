package com.rentflow.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DeliveryAnalyticsDTO {
    private int totalDeliveries = 0;
    private int onTimeDeliveries = 0;
    private int lateDeliveries = 0;
    private int failedDeliveries = 0;
    private int rescheduledDeliveries = 0;

    private BigDecimal onTimeDeliveryRate = BigDecimal.ZERO;
    private BigDecimal averageDeliveryDurationMinutes = BigDecimal.ZERO;
    private BigDecimal fleetVehicleUtilizationPercent = BigDecimal.ZERO;

    private List<NamedMetricDTO> deliveriesByStatus = new ArrayList<>();
    private List<NamedMetricDTO> deliveriesByVehicle = new ArrayList<>();
    private List<NamedMetricDTO> deliveriesByDriver = new ArrayList<>();

    public int getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(int totalDeliveries) { this.totalDeliveries = totalDeliveries; }

    public int getOnTimeDeliveries() { return onTimeDeliveries; }
    public void setOnTimeDeliveries(int onTimeDeliveries) { this.onTimeDeliveries = onTimeDeliveries; }

    public int getLateDeliveries() { return lateDeliveries; }
    public void setLateDeliveries(int lateDeliveries) { this.lateDeliveries = lateDeliveries; }

    public int getFailedDeliveries() { return failedDeliveries; }
    public void setFailedDeliveries(int failedDeliveries) { this.failedDeliveries = failedDeliveries; }

    public int getRescheduledDeliveries() { return rescheduledDeliveries; }
    public void setRescheduledDeliveries(int rescheduledDeliveries) { this.rescheduledDeliveries = rescheduledDeliveries; }

    public BigDecimal getOnTimeDeliveryRate() { return onTimeDeliveryRate; }
    public void setOnTimeDeliveryRate(BigDecimal onTimeDeliveryRate) { this.onTimeDeliveryRate = onTimeDeliveryRate; }

    public BigDecimal getAverageDeliveryDurationMinutes() { return averageDeliveryDurationMinutes; }
    public void setAverageDeliveryDurationMinutes(BigDecimal averageDeliveryDurationMinutes) { this.averageDeliveryDurationMinutes = averageDeliveryDurationMinutes; }

    public BigDecimal getFleetVehicleUtilizationPercent() { return fleetVehicleUtilizationPercent; }
    public void setFleetVehicleUtilizationPercent(BigDecimal fleetVehicleUtilizationPercent) { this.fleetVehicleUtilizationPercent = fleetVehicleUtilizationPercent; }

    public List<NamedMetricDTO> getDeliveriesByStatus() { return deliveriesByStatus; }
    public void setDeliveriesByStatus(List<NamedMetricDTO> deliveriesByStatus) { this.deliveriesByStatus = deliveriesByStatus; }

    public List<NamedMetricDTO> getDeliveriesByVehicle() { return deliveriesByVehicle; }
    public void setDeliveriesByVehicle(List<NamedMetricDTO> deliveriesByVehicle) { this.deliveriesByVehicle = deliveriesByVehicle; }

    public List<NamedMetricDTO> getDeliveriesByDriver() { return deliveriesByDriver; }
    public void setDeliveriesByDriver(List<NamedMetricDTO> deliveriesByDriver) { this.deliveriesByDriver = deliveriesByDriver; }
}
