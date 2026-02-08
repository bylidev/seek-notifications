package co.seekglobal.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Notification {

    @Builder.Default
    private final String id = UUID.randomUUID().toString();

    private final String recipient;

    private final String subject;

    private final String body;

    private final String templateId;

    private final Map<String, Object> templateVariables;

    private final Map<String, Object> metadata;

    @Builder.Default
    private final Priority priority = Priority.NORMAL;

    @Builder.Default
    private final OffsetDateTime createdAt = OffsetDateTime.now();

    private final OffsetDateTime scheduledAt;

    private final Integer ttlSeconds;

    private final String from;

    private final String replyTo;
}
