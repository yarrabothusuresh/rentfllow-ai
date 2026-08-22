package com.rentflow.notification.provider;

import com.rentflow.notification.model.NotificationChannel;

public interface EmailNotificationProvider extends NotificationChannelProvider {
    @Override
    default NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}
