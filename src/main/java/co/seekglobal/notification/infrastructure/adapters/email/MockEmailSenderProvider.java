package co.seekglobal.notification.infrastructure.adapters.email;

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
public final class MockEmailSenderProvider implements NotificationSenderProvider, ValidableSender {

    private static final Logger log = LoggerFactory.getLogger(MockEmailSenderProvider.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private MockEmailSenderProvider() {
    }

    @Override
    public NotificationResult send(Notification notification) {
        String messageId = UUID.randomUUID().toString();

        log.info("[EMAIL] To: {} | Subject: {} | Body: {} | MessageId: {}", notification.getRecipient(), notification.getSubject(), notification.getBody(), messageId);

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

    @Override
    public List<ValidationError> validate(Notification notification) {
        List<ValidationError> errors = new ArrayList<>();

        if (notification.getRecipient() == null || notification.getRecipient().isBlank()) {
            errors.add(ValidationError.of("recipient", "Email recipient is required"));
        } else if (!EMAIL_PATTERN.matcher(notification.getRecipient()).matches()) {
            errors.add(ValidationError.of("recipient", "Invalid email format: " + notification.getRecipient(), "INVALID_EMAIL_FORMAT"));
        }

        if ((notification.getBody() == null || notification.getBody().isBlank()) && (notification.getTemplateId() == null || notification.getTemplateId().isBlank())) {
            errors.add(ValidationError.of("body", "Email body or template is required"));
        }

        return errors;
    }
}
