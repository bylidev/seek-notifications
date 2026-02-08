package co.seekglobal.notification.application.inbound;

import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.Map;

@Builder
public record SendNotificationRequest(
        @NotNull(message = "Channel is required")
        NotificationChannel channel,
        @NotBlank(message = "Recipient is required")
        String recipient,
        String subject,
        String body,
        String templateId,
        Map<String, Object> templateVariables,
        Map<String, Object> metadata,
        Priority priority,
        OffsetDateTime scheduledAt,
        Integer ttlSeconds,
        String from,
        String replyTo
) {
}
