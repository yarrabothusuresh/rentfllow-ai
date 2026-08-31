package com.rentflow.warehouse.dto;

public class WarehouseMetricsDTO {
    private long ordersPickedToday;
    private double averagePickDurationMinutes;
    private double itemsPickedPerHour;
    private long ordersPackedToday;
    private double averagePackDurationMinutes;
    private double averageLoadDurationMinutes;
    private double exceptionsPer100Orders;
    private long activeExceptionsCount;
    private long readyForDeliveryCount;

    public long getOrdersPickedToday() { return ordersPickedToday; }
    public void setOrdersPickedToday(long ordersPickedToday) { this.ordersPickedToday = ordersPickedToday; }

    public double getAveragePickDurationMinutes() { return averagePickDurationMinutes; }
    public void setAveragePickDurationMinutes(double averagePickDurationMinutes) { this.averagePickDurationMinutes = averagePickDurationMinutes; }

    public double getItemsPickedPerHour() { return itemsPickedPerHour; }
    public void setItemsPickedPerHour(double itemsPickedPerHour) { this.itemsPickedPerHour = itemsPickedPerHour; }

    public long getOrdersPackedToday() { return ordersPackedToday; }
    public void setOrdersPackedToday(long ordersPackedToday) { this.ordersPackedToday = ordersPackedToday; }

    public double getAveragePackDurationMinutes() { return averagePackDurationMinutes; }
    public void setAveragePackDurationMinutes(double averagePackDurationMinutes) { this.averagePackDurationMinutes = averagePackDurationMinutes; }

    public double getAverageLoadDurationMinutes() { return averageLoadDurationMinutes; }
    public void setAverageLoadDurationMinutes(double averageLoadDurationMinutes) { this.averageLoadDurationMinutes = averageLoadDurationMinutes; }

    public double getExceptionsPer100Orders() { return exceptionsPer100Orders; }
    public void setExceptionsPer100Orders(double exceptionsPer100Orders) { this.exceptionsPer100Orders = exceptionsPer100Orders; }

    public long getActiveExceptionsCount() { return activeExceptionsCount; }
    public void setActiveExceptionsCount(long activeExceptionsCount) { this.activeExceptionsCount = activeExceptionsCount; }

    public long getReadyForDeliveryCount() { return readyForDeliveryCount; }
    public void setReadyForDeliveryCount(long readyForDeliveryCount) { this.readyForDeliveryCount = readyForDeliveryCount; }
}
