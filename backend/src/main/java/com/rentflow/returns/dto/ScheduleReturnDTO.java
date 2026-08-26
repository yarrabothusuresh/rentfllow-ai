package com.rentflow.returns.dto;

import java.time.LocalDate;

public class ScheduleReturnDTO {
    private LocalDate date;
    private String startTime;
    private String endTime;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
