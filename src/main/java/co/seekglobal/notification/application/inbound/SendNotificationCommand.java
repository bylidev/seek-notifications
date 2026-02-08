package co.seekglobal.notification.application.inbound;

import co.seekglobal.notification.domain.NotificationResult;

import java.util.concurrent.Executor;


public interface SendNotificationCommand {

    NotificationResult send(SendNotificationRequest command);

    void sendAsync(SendNotificationRequest command, Executor executor);
}
