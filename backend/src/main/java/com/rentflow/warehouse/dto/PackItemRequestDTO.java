package com.rentflow.warehouse.dto;

public class PackItemRequestDTO {

    private int quantity;
    private String notes;

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
