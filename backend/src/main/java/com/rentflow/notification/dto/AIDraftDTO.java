package com.rentflow.notification.dto;

import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationType;

public class AIDraftDTO {
    private String subject;
    private String body;
    private NotificationType notificationType;
    private NotificationChannel channel;

    public AIDraftDTO() {}

    public AIDraftDTO(String subject, String body, NotificationType notificationType, NotificationChannel channel) {
        this.subject = subject;
        this.body = body;
        this.notificationType = notificationType;
        this.channel = channel;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
}
