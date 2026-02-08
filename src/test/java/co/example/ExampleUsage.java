package co.example;

import co.seekglobal.notification.application.inbound.SendNotificationRequest;
import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.NotificationResult;
import co.seekglobal.notification.domain.Priority;
import co.seekglobal.notification.infrastructure.adapters.email.MockEmailFallback;
import co.seekglobal.notification.infrastructure.adapters.email.MockEmailSenderProvider;
import co.seekglobal.notification.infrastructure.adapters.push.MockPushSenderProvider;
import co.seekglobal.notification.infrastructure.adapters.sms.MockSmsSenderProvider;
import co.seekglobal.notification.infrastructure.config.SeekNotificationBuilder;

import java.time.OffsetDateTime;
import java.util.Map;

public class ExampleUsage {

    public static void main(String[] args) {
        var seekNotification = SeekNotificationBuilder.builder()
                .provider(MockEmailSenderProvider.factory().build())
                .provider(MockEmailFallback.factory().build())
                .provider(MockSmsSenderProvider.factory().build())
                .provider(MockPushSenderProvider.factory().build())
                .build();

        SendNotificationRequest emailRequest = SendNotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("usuario@ejemplo.com")
                .subject("Bienvenido a Seek Notifications")
                .body("Hola, esta es una notificación de ejemplo.")
                .priority(Priority.NORMAL)
                .metadata(Map.of("source", "example"))
                .build();

        NotificationResult emailResult = seekNotification.send(emailRequest);
        System.out.println("Resultado del envío por email: " + emailResult);

        SendNotificationRequest smsRequest = SendNotificationRequest.builder()
                .channel(NotificationChannel.SMS)
                .recipient("+1234567890")
                .body("Hola, esta es una notificación SMS de ejemplo.")
                .priority(Priority.HIGH)
                .build();

        NotificationResult smsResult = seekNotification.send(smsRequest);
        System.out.println("Resultado del envío por SMS: " + smsResult);

        SendNotificationRequest pushRequest = SendNotificationRequest.builder()
                .channel(NotificationChannel.PUSH)
                .recipient("device-token-123")
                .body("Hola, esta es una notificación push de ejemplo.")
                .priority(Priority.NORMAL)
                .scheduledAt(OffsetDateTime.now().plusMinutes(5))
                .ttlSeconds(3600)
                .build();

        NotificationResult pushResult = seekNotification.send(pushRequest);
        System.out.println("Resultado del envío push: " + pushResult);
    }
}
