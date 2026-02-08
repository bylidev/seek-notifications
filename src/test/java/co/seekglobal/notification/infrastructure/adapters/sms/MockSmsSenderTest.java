package co.seekglobal.notification.infrastructure.adapters.sms;

import co.seekglobal.notification.application.outbound.ValidableSender;
import co.seekglobal.notification.domain.Notification;
import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.NotificationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static co.seekglobal.notification.infrastructure.config.Providers.Sms.SMS_PROVIDER_1;
import static org.junit.jupiter.api.Assertions.*;

class MockSmsSenderTest {

    private final MockSmsSenderProvider sender = MockSmsSenderProvider.factory().build();

    @Test
    void send_shouldReturnSuccessResult() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("+1234567890")
                .body("Test SMS Body")
                .build();

        // Act
        NotificationResult result = sender.send(notification);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(notification.getId(), result.getNotificationId());
        assertNotNull(result.getProviderMessageId());
        assertTrue(result.getProviderMessageId().startsWith("SM"));
        assertEquals(NotificationChannel.SMS, result.getChannel());
        assertEquals(SMS_PROVIDER_1.name(), result.getProviderName());
    }

    @Test
    void getChannel_shouldReturnSms() {
        // Act & Assert
        assertEquals(NotificationChannel.SMS, sender.getChannel());
    }

    @Test
    void getProviderName_shouldReturnMockSMS() {
        // Act & Assert
        assertEquals(SMS_PROVIDER_1.name(), sender.getProviderName());
    }

    @Test
    void validate_shouldReturnEmptyList_whenValidNotification() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("+1234567890")
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
        assertEquals("Phone number is required", errors.get(0).message());
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
        assertEquals("Phone number is required", errors.get(0).message());
    }

    @Test
    void validate_shouldReturnError_whenInvalidPhoneFormat() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("invalid-phone")
                .body("Test Body")
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("recipient", errors.get(0).field());
        assertEquals("INVALID_PHONE_FORMAT", errors.get(0).code());
    }

    @Test
    void validate_shouldReturnError_whenBodyAndTemplateAreNull() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("+1234567890")
                .body(null)
                .templateId(null)
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("body", errors.get(0).field());
        assertEquals("SMS body or template is required", errors.get(0).message());
    }

    @Test
    void validate_shouldReturnError_whenBodyExceedsMaxLength() {
        // Arrange
        String longBody = "a".repeat(1601);
        Notification notification = Notification.builder()
                .recipient("+1234567890")
                .body(longBody)
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("body", errors.get(0).field());
        assertEquals("SMS_BODY_TOO_LONG", errors.get(0).code());
    }
}
