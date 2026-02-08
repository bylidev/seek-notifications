package co.seekglobal.notification.infrastructure.adapters.push;

import co.seekglobal.notification.application.outbound.NotificationSenderProvider;
import co.seekglobal.notification.application.outbound.ValidableSender;
import co.seekglobal.notification.domain.Notification;
import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.NotificationResult;
import co.seekglobal.notification.infrastructure.config.Providers;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder(builderMethodName = "factory")
public final class MockPushSenderProvider implements NotificationSenderProvider, ValidableSender {

    private static final Logger log = LoggerFactory.getLogger(MockPushSenderProvider.class);
    private static final int FCM_TOKEN_MIN_LENGTH = 100;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_BODY_LENGTH = 4096;

    private MockPushSenderProvider() {
    }

    @Override
    public NotificationResult send(Notification notification) {
        String messageId = UUID.randomUUID().toString();

        log.info("[PUSH] To: {} | Title: {} | Body: {} | MessageId: {}",
                notification.getRecipient().substring(0, Math.min(20, notification.getRecipient().length())) + "...",
                notification.getSubject(),
                notification.getBody(),
                messageId);

        return NotificationResult.success(
                notification.getId(),
                messageId,
                NotificationChannel.PUSH,
                getProviderName()
        );
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public String getProviderName() {
        return Providers.Push.PUSH_PROVIDER_1.name();
    }

    @Override
    public List<ValidationError> validate(Notification notification) {
        List<ValidationError> errors = new ArrayList<>();

        if (notification.getRecipient() == null || notification.getRecipient().isBlank()) {
            errors.add(ValidationError.of("recipient", "Device token is required"));
        } else if (notification.getRecipient().length() < FCM_TOKEN_MIN_LENGTH) {
            errors.add(ValidationError.of("recipient", "Device token appears to be invalid (too short)", "INVALID_DEVICE_TOKEN"));
        }

        if ((notification.getBody() == null || notification.getBody().isBlank())
                && (notification.getTemplateId() == null || notification.getTemplateId().isBlank())) {
            errors.add(ValidationError.of("body", "Push notification body or template is required"));
        } else if (notification.getBody() != null && notification.getBody().length() > MAX_BODY_LENGTH) {
            errors.add(ValidationError.of("body", String.format("Push body exceeds maximum length of %d characters", MAX_BODY_LENGTH), "PUSH_BODY_TOO_LONG"));
        }

        if (notification.getSubject() != null && notification.getSubject().length() > MAX_TITLE_LENGTH) {
            errors.add(ValidationError.of("subject", String.format("Push title exceeds maximum length of %d characters", MAX_TITLE_LENGTH), "PUSH_TITLE_TOO_LONG"));
        }

        return errors;
    }
}
