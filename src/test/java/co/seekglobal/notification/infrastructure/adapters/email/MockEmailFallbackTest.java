package co.seekglobal.notification.infrastructure.adapters.email;

import co.seekglobal.notification.domain.Notification;
import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.NotificationResult;
import org.junit.jupiter.api.Test;

import static co.seekglobal.notification.infrastructure.config.Providers.Email.EMAIL_PROVIDER_2;
import static org.junit.jupiter.api.Assertions.*;

class MockEmailFallbackTest {

    private final MockEmailFallback sender = MockEmailFallback.factory().build();

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
    void getProviderName_shouldReturnMockEmailFallback() {
        // Act & Assert
        assertEquals(EMAIL_PROVIDER_2.name(), sender.getProviderName());
    }
}
