package com.rentflow.notification.provider;

import com.rentflow.notification.model.NotificationChannel;

public interface SmsNotificationProvider extends NotificationChannelProvider {
    @Override
    default NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
}
