package co.seekglobal.notification.infrastructure.adapters.push;

import co.seekglobal.notification.application.outbound.ValidableSender;
import co.seekglobal.notification.domain.Notification;
import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.NotificationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static co.seekglobal.notification.infrastructure.config.Providers.Push.PUSH_PROVIDER_1;
import static org.junit.jupiter.api.Assertions.*;

class MockPushSenderTest {

    private final MockPushSenderProvider sender = MockPushSenderProvider.factory().build();

    @Test
    void send_shouldReturnSuccessResult() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("fcm_token_1234567890".repeat(10)) // Make it long enough
                .subject("Test Title")
                .body("Test Body")
                .build();

        // Act
        NotificationResult result = sender.send(notification);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(notification.getId(), result.getNotificationId());
        assertNotNull(result.getProviderMessageId());
        assertEquals(NotificationChannel.PUSH, result.getChannel());
        assertEquals(PUSH_PROVIDER_1.name(), result.getProviderName());
    }

    @Test
    void getChannel_shouldReturnPush() {
        // Act & Assert
        assertEquals(NotificationChannel.PUSH, sender.getChannel());
    }

    @Test
    void getProviderName_shouldReturnMockPush() {
        // Act & Assert
        assertEquals(PUSH_PROVIDER_1.name(), sender.getProviderName());
    }

    @Test
    void validate_shouldReturnEmptyList_whenValidNotification() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("fcm_token_1234567890".repeat(10))
                .subject("Test Title")
                .body("Test Body")
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertTrue(errors.isEmpty());
    }

    @Test
    void validate_shouldReturnError_whenRecipientIsNull() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient(null)
                .body("Test Body")
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("recipient", errors.get(0).field());
        assertEquals("Device token is required", errors.get(0).message());
    }

    @Test
    void validate_shouldReturnError_whenRecipientIsBlank() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("")
                .body("Test Body")
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("recipient", errors.get(0).field());
        assertEquals("Device token is required", errors.get(0).message());
    }

    @Test
    void validate_shouldReturnError_whenRecipientTooShort() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("short")
                .body("Test Body")
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("recipient", errors.get(0).field());
        assertEquals("INVALID_DEVICE_TOKEN", errors.get(0).code());
    }

    @Test
    void validate_shouldReturnError_whenBodyAndTemplateAreNull() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("fcm_token_1234567890".repeat(10))
                .body(null)
                .templateId(null)
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("body", errors.get(0).field());
        assertEquals("Push notification body or template is required", errors.get(0).message());
    }

    @Test
    void validate_shouldReturnError_whenBodyExceedsMaxLength() {
        // Arrange
        String longBody = "a".repeat(4097);
        Notification notification = Notification.builder()
                .recipient("fcm_token_1234567890".repeat(10))
                .body(longBody)
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("body", errors.get(0).field());
        assertEquals("PUSH_BODY_TOO_LONG", errors.get(0).code());
    }

    @Test
    void validate_shouldReturnError_whenSubjectExceedsMaxLength() {
        // Arrange
        String longTitle = "a".repeat(101);
        Notification notification = Notification.builder()
                .recipient("fcm_token_1234567890".repeat(10))
                .subject(longTitle)
                .body("Test Body")
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("subject", errors.get(0).field());
        assertEquals("PUSH_TITLE_TOO_LONG", errors.get(0).code());
    }
}
