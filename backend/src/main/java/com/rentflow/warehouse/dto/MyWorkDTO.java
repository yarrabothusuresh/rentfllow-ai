package com.rentflow.warehouse.dto;

import java.util.ArrayList;
import java.util.List;

public class MyWorkDTO {
    private List<PickListDTO> assignedPickLists = new ArrayList<>();
    private List<PackListDTO> assignedPackLists = new ArrayList<>();
    private List<LoadListDTO> assignedLoadLists = new ArrayList<>();
    private List<WarehouseExceptionDTO> assignedExceptions = new ArrayList<>();
    private int urgentTasksCount;

    public List<PickListDTO> getAssignedPickLists() { return assignedPickLists; }
    public void setAssignedPickLists(List<PickListDTO> assignedPickLists) { this.assignedPickLists = assignedPickLists; }

    public List<PackListDTO> getAssignedPackLists() { return assignedPackLists; }
    public void setAssignedPackLists(List<PackListDTO> assignedPackLists) { this.assignedPackLists = assignedPackLists; }

    public List<LoadListDTO> getAssignedLoadLists() { return assignedLoadLists; }
    public void setAssignedLoadLists(List<LoadListDTO> assignedLoadLists) { this.assignedLoadLists = assignedLoadLists; }

    public List<WarehouseExceptionDTO> getAssignedExceptions() { return assignedExceptions; }
    public void setAssignedExceptions(List<WarehouseExceptionDTO> assignedExceptions) { this.assignedExceptions = assignedExceptions; }

    public int getUrgentTasksCount() { return urgentTasksCount; }
    public void setUrgentTasksCount(int urgentTasksCount) { this.urgentTasksCount = urgentTasksCount; }
}
