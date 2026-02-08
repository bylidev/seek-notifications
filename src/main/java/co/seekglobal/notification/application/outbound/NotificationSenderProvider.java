package co.seekglobal.notification.application.outbound;

import co.seekglobal.notification.domain.Notification;
import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.NotificationResult;

public interface NotificationSenderProvider {
    NotificationResult send(Notification notification);

    NotificationChannel getChannel();

    String getProviderName();
}
