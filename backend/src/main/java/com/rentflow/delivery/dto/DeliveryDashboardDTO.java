package com.rentflow.delivery.dto;

import java.util.List;

public class DeliveryDashboardDTO {
    private long todaysDeliveries;
    private long unassigned;
    private long scheduled;
    private long outForDelivery;
    private long completed;
    private long urgent;
    private long failed;
    private List<DeliveryDTO> todaysQueue;

    public long getTodaysDeliveries() { return todaysDeliveries; }
    public void setTodaysDeliveries(long todaysDeliveries) { this.todaysDeliveries = todaysDeliveries; }
    public long getUnassigned() { return unassigned; }
    public void setUnassigned(long unassigned) { this.unassigned = unassigned; }
    public long getScheduled() { return scheduled; }
    public void setScheduled(long scheduled) { this.scheduled = scheduled; }
    public long getOutForDelivery() { return outForDelivery; }
    public void setOutForDelivery(long outForDelivery) { this.outForDelivery = outForDelivery; }
    public long getCompleted() { return completed; }
    public void setCompleted(long completed) { this.completed = completed; }
    public long getUrgent() { return urgent; }
    public void setUrgent(long urgent) { this.urgent = urgent; }
    public long getFailed() { return failed; }
    public void setFailed(long failed) { this.failed = failed; }
    public List<DeliveryDTO> getTodaysQueue() { return todaysQueue; }
    public void setTodaysQueue(List<DeliveryDTO> todaysQueue) { this.todaysQueue = todaysQueue; }
}
