package com.rentflow.warehouse.dto;

public class PickItemRequestDTO {

    private int quantity;
    private String notes;
    private boolean isShortage;

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isShortage() { return isShortage; }
    public void setShortage(boolean shortage) { isShortage = shortage; }
}
