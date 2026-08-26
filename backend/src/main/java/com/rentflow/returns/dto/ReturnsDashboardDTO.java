package com.rentflow.returns.dto;

import java.util.List;

public class ReturnsDashboardDTO {

    private long todaysPickups;
    private long unassigned;
    private long scheduled;
    private long outForPickup;
    private long pickedUp;
    private long pendingInspection;
    private long missingItems;
    private long damagedItems;

    private List<ReturnOrderDTO> todaysQueue;

    public long getTodaysPickups() { return todaysPickups; }
    public void setTodaysPickups(long todaysPickups) { this.todaysPickups = todaysPickups; }

    public long getUnassigned() { return unassigned; }
    public void setUnassigned(long unassigned) { this.unassigned = unassigned; }

    public long getScheduled() { return scheduled; }
    public void setScheduled(long scheduled) { this.scheduled = scheduled; }

    public long getOutForPickup() { return outForPickup; }
    public void setOutForPickup(long outForPickup) { this.outForPickup = outForPickup; }

    public long getPickedUp() { return pickedUp; }
    public void setPickedUp(long pickedUp) { this.pickedUp = pickedUp; }

    public long getPendingInspection() { return pendingInspection; }
    public void setPendingInspection(long pendingInspection) { this.pendingInspection = pendingInspection; }

    public long getMissingItems() { return missingItems; }
    public void setMissingItems(long missingItems) { this.missingItems = missingItems; }

    public long getDamagedItems() { return damagedItems; }
    public void setDamagedItems(long damagedItems) { this.damagedItems = damagedItems; }

    public List<ReturnOrderDTO> getTodaysQueue() { return todaysQueue; }
    public void setTodaysQueue(List<ReturnOrderDTO> todaysQueue) { this.todaysQueue = todaysQueue; }
}
