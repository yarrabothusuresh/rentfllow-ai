package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.WarehouseExceptionResolution;

public class ResolveExceptionRequestDTO {
    private WarehouseExceptionResolution resolution;
    private String resolutionNotes;

    public WarehouseExceptionResolution getResolution() { return resolution; }
    public void setResolution(WarehouseExceptionResolution resolution) { this.resolution = resolution; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}
