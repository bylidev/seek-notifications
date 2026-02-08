package co.seekglobal.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class NotificationResult {

    private final boolean success;

    private final String notificationId;

    private final String providerMessageId;

    private final NotificationChannel channel;

    private final String providerName;

    private final ErrorCode errorCode;

    private final String errorMessage;

    @Builder.Default
    private final OffsetDateTime processedAt = OffsetDateTime.now();

    private final Map<String, Object> providerResponse;

    public static NotificationResult success(String notificationId,
                                              String providerMessageId,
                                              NotificationChannel channel,
                                              String providerName) {
        return NotificationResult.builder()
                .success(true)
                .notificationId(notificationId)
                .providerMessageId(providerMessageId)
                .channel(channel)
                .providerName(providerName)
                .build();
    }

    public static NotificationResult failure(String notificationId,
                                              NotificationChannel channel,
                                              String providerName,
                                              ErrorCode errorCode,
                                              String errorMessage) {
        return NotificationResult.builder()
                .success(false)
                .notificationId(notificationId)
                .channel(channel)
                .providerName(providerName)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    public static NotificationResult validationFailure(String notificationId,
                                                        NotificationChannel channel,
                                                        String errorMessage) {
        return NotificationResult.builder()
                .success(false)
                .notificationId(notificationId)
                .channel(channel)
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .errorMessage(errorMessage)
                .build();
    }
}
