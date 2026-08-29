package com.rentflow.calendar.dto;

import com.rentflow.calendar.model.CalendarEventType;

import java.time.LocalDateTime;

public class RescheduleRequestDTO {
    private CalendarEventType eventType;
    private String referenceId;
    private LocalDateTime newStart;
    private LocalDateTime newEnd;

    public RescheduleRequestDTO() {}

    public CalendarEventType getEventType() { return eventType; }
    public void setEventType(CalendarEventType eventType) { this.eventType = eventType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public LocalDateTime getNewStart() { return newStart; }
    public void setNewStart(LocalDateTime newStart) { this.newStart = newStart; }

    public LocalDateTime getNewEnd() { return newEnd; }
    public void setNewEnd(LocalDateTime newEnd) { this.newEnd = newEnd; }
}
