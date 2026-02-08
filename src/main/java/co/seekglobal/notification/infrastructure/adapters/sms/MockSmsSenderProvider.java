package co.seekglobal.notification.infrastructure.adapters.sms;

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
import java.util.regex.Pattern;

@Builder(builderMethodName = "factory")
public final class MockSmsSenderProvider implements NotificationSenderProvider, ValidableSender {

    private static final Logger log = LoggerFactory.getLogger(MockSmsSenderProvider.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{6,14}$");
    private static final int MAX_SMS_LENGTH = 1600;

    private MockSmsSenderProvider() {
    }

    @Override
    public NotificationResult send(Notification notification) {
        String messageId = "SM" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        log.info("[SMS] To: {} | Body: {} | MessageId: {}",
                notification.getRecipient(),
                notification.getBody(),
                messageId);

        return NotificationResult.success(
                notification.getId(),
                messageId,
                NotificationChannel.SMS,
                getProviderName()
        );
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public String getProviderName() {
        return Providers.Sms.SMS_PROVIDER_1.name();
    }

    @Override
    public List<ValidationError> validate(Notification notification) {
        List<ValidationError> errors = new ArrayList<>();

        if (notification.getRecipient() == null || notification.getRecipient().isBlank()) {
            errors.add(ValidationError.of("recipient", "Phone number is required"));
        } else if (!PHONE_PATTERN.matcher(notification.getRecipient()).matches()) {
            errors.add(ValidationError.of("recipient", "Invalid phone number format: " + notification.getRecipient(), "INVALID_PHONE_FORMAT"));
        }

        if ((notification.getBody() == null || notification.getBody().isBlank())
                && (notification.getTemplateId() == null || notification.getTemplateId().isBlank())) {
            errors.add(ValidationError.of("body", "SMS body or template is required"));
        } else if (notification.getBody() != null && notification.getBody().length() > MAX_SMS_LENGTH) {
            errors.add(ValidationError.of("body", String.format("SMS body exceeds maximum length of %d characters", MAX_SMS_LENGTH), "SMS_BODY_TOO_LONG"));
        }

        return errors;
    }
}
