package com.rentflow.warehouse.dto;

import java.util.ArrayList;
import java.util.List;

public class WarehouseDashboardDTO {

    private long todaysOrders;
    private long readyToPick;
    private long picking;
    private long packing;
    private long readyForDelivery;
    private long shortItems;

    private List<WarehouseOrderDTO> todaysPriorities = new ArrayList<>();

    public long getTodaysOrders() { return todaysOrders; }
    public void setTodaysOrders(long todaysOrders) { this.todaysOrders = todaysOrders; }

    public long getReadyToPick() { return readyToPick; }
    public void setReadyToPick(long readyToPick) { this.readyToPick = readyToPick; }

    public long getPicking() { return picking; }
    public void setPicking(long picking) { this.picking = picking; }

    public long getPacking() { return packing; }
    public void setPacking(long packing) { this.packing = packing; }

    public long getReadyForDelivery() { return readyForDelivery; }
    public void setReadyForDelivery(long readyForDelivery) { this.readyForDelivery = readyForDelivery; }

    public long getShortItems() { return shortItems; }
    public void setShortItems(long shortItems) { this.shortItems = shortItems; }

    public List<WarehouseOrderDTO> getTodaysPriorities() { return todaysPriorities; }
    public void setTodaysPriorities(List<WarehouseOrderDTO> todaysPriorities) { this.todaysPriorities = todaysPriorities; }
}
