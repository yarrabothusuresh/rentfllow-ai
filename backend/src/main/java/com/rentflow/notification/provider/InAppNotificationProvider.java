package com.rentflow.notification.provider;

import com.rentflow.notification.model.NotificationChannel;

public interface InAppNotificationProvider extends NotificationChannelProvider {
    @Override
    default NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }
}
