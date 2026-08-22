package com.rentflow.notification.dto;

public class NotificationUnreadCountDTO {
    private long count;

    public NotificationUnreadCountDTO() {}

    public NotificationUnreadCountDTO(long count) {
        this.count = count;
    }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
