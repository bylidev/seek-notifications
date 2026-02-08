package co.seekglobal.notification.infrastructure.adapters.email;

import co.seekglobal.notification.application.outbound.NotificationSenderProvider;
import co.seekglobal.notification.domain.Notification;
import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.NotificationResult;
import co.seekglobal.notification.infrastructure.config.Providers;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Builder(builderMethodName = "factory")
public final class MockEmailFallback implements NotificationSenderProvider {

    private static final Logger log = LoggerFactory.getLogger(MockEmailFallback.class);

    private MockEmailFallback(){}

    @Override
    public NotificationResult send(Notification notification) {
        String messageId = UUID.randomUUID().toString();

        log.info("[EMAIL FALLBACK] To: {} | Subject: {} | Body: {} | MessageId: {}", notification.getRecipient(), notification.getSubject(), notification.getBody(), messageId);

        return NotificationResult.success(notification.getId(), messageId, NotificationChannel.EMAIL, getProviderName());
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public String getProviderName() {
        return Providers.Email.EMAIL_PROVIDER_2.name();
    }

}

