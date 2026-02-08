package co.seekglobal.notification.application.usecase;

import co.seekglobal.notification.application.inbound.SendNotificationCommand;
import co.seekglobal.notification.application.inbound.SendNotificationRequest;
import co.seekglobal.notification.application.outbound.NotificationSenderProvider;
import co.seekglobal.notification.application.outbound.ValidableSender;
import co.seekglobal.notification.application.outbound.ValidableSender.ValidationError;
import co.seekglobal.notification.domain.ErrorCode;
import co.seekglobal.notification.domain.Notification;
import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.NotificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class SendNotificationUseCase implements SendNotificationCommand {

    private final Map<NotificationChannel, Set<NotificationSenderProvider>> senders;

    @Override
    public NotificationResult send(SendNotificationRequest request) {
        var notification = toNotification(request);
        var channel = request.channel();
        var channelSenders = getSenders(channel);

        if (channelSenders.isEmpty()) {
            return noSenderConfigured(channel);
        }

        return trySend(notification, channelSenders);
    }

    @Override
    public void sendAsync(SendNotificationRequest command, Executor executor) {
        CompletableFuture.runAsync(() -> send(command), executor)
                .exceptionally(ex -> {
                    log.error("Failed to send notification asynchronously: {}", ex.getMessage(), ex);
                    return null;
                });
    }

    private Set<NotificationSenderProvider> getSenders(NotificationChannel channel) {
        return senders.getOrDefault(channel, Set.of());
    }

    private NotificationResult noSenderConfigured(NotificationChannel channel) {
        return NotificationResult.failure(null, channel, null,
                ErrorCode.CHANNEL_NOT_CONFIGURED,
                "No sender configured for channel: " + channel);
    }

    private NotificationResult trySend(Notification notification, Set<NotificationSenderProvider> channelSenders) {
        return channelSenders.stream()
                .filter(sender -> isValidRequest(sender, notification))
                .map(sender -> sender.send(notification))
                .filter(NotificationResult::isSuccess)
                .findFirst()
                .orElseGet(() -> notifyFailure(notification));
    }

    private boolean isValidRequest(NotificationSenderProvider sender, Notification notification) {
        if (sender instanceof ValidableSender validator) {
            var e = validator.validate(notification);
            if (!e.isEmpty()) {
                log.error("Provider: {}, Notification: {}, Validation error: {}", sender.getClass(), notification.getId(),
                        e.stream().map(ValidationError::message).collect(Collectors.joining()));
                return false;
            }
        }
        return true;
    }

    private NotificationResult notifyFailure(Notification notification) {
        return NotificationResult.failure(
                notification.getId(), null, null,
                ErrorCode.PROVIDER_NOT_FOUND, "All senders failed");
    }

    private Notification toNotification(SendNotificationRequest request) {
        return Notification.builder()
                .recipient(request.recipient())
                .subject(request.subject())
                .body(request.body())
                .templateId(request.templateId())
                .templateVariables(request.templateVariables())
                .metadata(request.metadata())
                .priority(request.priority())
                .scheduledAt(request.scheduledAt())
                .ttlSeconds(request.ttlSeconds())
                .from(request.from())
                .replyTo(request.replyTo())
                .build();
    }
}
