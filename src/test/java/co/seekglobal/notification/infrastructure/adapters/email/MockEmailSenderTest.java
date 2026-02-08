package co.seekglobal.notification.infrastructure.adapters.email;

import co.seekglobal.notification.application.outbound.ValidableSender;
import co.seekglobal.notification.domain.Notification;
import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.NotificationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static co.seekglobal.notification.infrastructure.config.Providers.Email.EMAIL_PROVIDER_2;
import static org.junit.jupiter.api.Assertions.*;

class MockEmailSenderTest {

    private final MockEmailSenderProvider sender = MockEmailSenderProvider.factory().build();

    @Test
    void send_shouldReturnSuccessResult() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("test@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .build();

        // Act
        NotificationResult result = sender.send(notification);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(notification.getId(), result.getNotificationId());
        assertNotNull(result.getProviderMessageId());
        assertEquals(NotificationChannel.EMAIL, result.getChannel());
        assertEquals(EMAIL_PROVIDER_2.name(), result.getProviderName());
    }

    @Test
    void getChannel_shouldReturnEmail() {
        // Act & Assert
        assertEquals(NotificationChannel.EMAIL, sender.getChannel());
    }

    @Test
    void validate_shouldReturnEmptyList_whenValidNotification() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("test@example.com")
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
        assertEquals("Email recipient is required", errors.get(0).message());
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
        assertEquals("Email recipient is required", errors.get(0).message());
    }

    @Test
    void validate_shouldReturnError_whenInvalidEmailFormat() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("invalid-email")
                .body("Test Body")
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("recipient", errors.get(0).field());
        assertEquals("INVALID_EMAIL_FORMAT", errors.get(0).code());
    }

    @Test
    void validate_shouldReturnError_whenBodyAndTemplateAreNull() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("test@example.com")
                .body(null)
                .templateId(null)
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("body", errors.get(0).field());
        assertEquals("Email body or template is required", errors.get(0).message());
    }

    @Test
    void validate_shouldReturnError_whenBodyAndTemplateAreBlank() {
        // Arrange
        Notification notification = Notification.builder()
                .recipient("test@example.com")
                .body("")
                .templateId("")
                .build();

        // Act
        List<ValidableSender.ValidationError> errors = sender.validate(notification);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("body", errors.get(0).field());
        assertEquals("Email body or template is required", errors.get(0).message());
    }
}
