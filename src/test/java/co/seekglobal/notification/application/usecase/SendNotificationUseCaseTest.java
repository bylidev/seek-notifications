package co.seekglobal.notification.application.usecase;

import co.seekglobal.notification.application.inbound.SendNotificationRequest;
import co.seekglobal.notification.application.outbound.NotificationSenderProvider;
import co.seekglobal.notification.application.outbound.ValidableSender;
import co.seekglobal.notification.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SendNotificationUseCaseTest {

    @Mock
    private NotificationSenderProvider mockSender1;

    @Mock
    private NotificationSenderProvider mockSender2;

    @Mock(extraInterfaces = ValidableSender.class)
    private NotificationSenderProvider mockValidableSender;

    private SendNotificationUseCase useCase;


    @Test
    void shouldReturnFailureWhenNoSendersConfiguredForChannel() {
        // Given
        Map<NotificationChannel, Set<NotificationSenderProvider>> senders = Map.of();
        useCase = new SendNotificationUseCase(senders);

        var request = SendNotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .subject("Test")
                .body("Hello")
                .build();

        // When
        var result = useCase.send(request);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.CHANNEL_NOT_CONFIGURED, result.getErrorCode());
        assertEquals("No sender configured for channel: EMAIL", result.getErrorMessage());
    }

    @Test
    void shouldReturnFailureWhenAllSendersFailValidation() {
        // Given
        ValidableSender validator = (ValidableSender) mockValidableSender;
        when(validator.validate(any(Notification.class)))
                .thenReturn(List.of(ValidableSender.ValidationError.of("recipient", "Invalid email")));

        Set<NotificationSenderProvider> emailSenders = Set.of(mockValidableSender);
        Map<NotificationChannel, Set<NotificationSenderProvider>> senders = Map.of(NotificationChannel.EMAIL, emailSenders);
        useCase = new SendNotificationUseCase(senders);

        var request = SendNotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("invalid-email")
                .subject("Test")
                .body("Hello")
                .build();

        // When
        var result = useCase.send(request);

        // Then
        assertFalse(result.isSuccess());
        verify((ValidableSender) mockValidableSender).validate(any());
        assertEquals(ErrorCode.PROVIDER_NOT_FOUND, result.getErrorCode());
        assertEquals("All senders failed", result.getErrorMessage());
    }

    @Test
    void shouldReturnSuccessWhenFirstValidSenderSucceeds() {
        // Given
        var successResult = NotificationResult.success("notif-123", "msg-456", NotificationChannel.EMAIL, "MockSender");

        ValidableSender validator = (ValidableSender) mockValidableSender;
        when(validator.validate(any(Notification.class))).thenReturn(List.of());
        when(mockValidableSender.send(any(Notification.class))).thenReturn(successResult);

        Set<NotificationSenderProvider> emailSenders = new LinkedHashSet<>();
        emailSenders.add(mockValidableSender);
        emailSenders.add(mockSender1); // This won't be called

        Map<NotificationChannel, Set<NotificationSenderProvider>> senders = Map.of(NotificationChannel.EMAIL, emailSenders);
        useCase = new SendNotificationUseCase(senders);

        var request = SendNotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .subject("Test")
                .body("Hello")
                .build();

        // When
        var result = useCase.send(request);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("notif-123", result.getNotificationId());
        assertEquals("msg-456", result.getProviderMessageId());
    }

    @Test
    void shouldSkipInvalidSenderAndUseNextValidOne() {
        // Given
        var successResult = NotificationResult.success("notif-123", "msg-456", NotificationChannel.EMAIL, "MockSender2");

        ValidableSender validator = (ValidableSender) mockValidableSender;
        when(validator.validate(any(Notification.class)))
                .thenReturn(List.of(ValidableSender.ValidationError.of("recipient", "Invalid")));
        when(mockSender1.send(any(Notification.class))).thenReturn(successResult);

        Set<NotificationSenderProvider> emailSenders = new LinkedHashSet<>();
        emailSenders.add(mockValidableSender); // Fails validation
        emailSenders.add(mockSender1); // Succeeds

        Map<NotificationChannel, Set<NotificationSenderProvider>> senders = Map.of(NotificationChannel.EMAIL, emailSenders);
        useCase = new SendNotificationUseCase(senders);

        var request = SendNotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .subject("Test")
                .body("Hello")
                .build();

        // When
        var result = useCase.send(request);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("msg-456", result.getProviderMessageId());
    }

    @Test
    void shouldReturnFailureWhenAllSendersFailAfterValidation() {
        // Given
        var failureResult1 = NotificationResult.failure("notif-123", NotificationChannel.EMAIL, "MockSender1", ErrorCode.PROVIDER_NOT_FOUND, "Send failed");
        var failureResult2 = NotificationResult.failure("notif-123", NotificationChannel.EMAIL, "MockSender2", ErrorCode.PROVIDER_NOT_FOUND, "Send failed");

        when(mockSender1.send(any(Notification.class))).thenReturn(failureResult1);
        when(mockSender2.send(any(Notification.class))).thenReturn(failureResult2);

        Set<NotificationSenderProvider> emailSenders = new LinkedHashSet<>();
        emailSenders.add(mockSender1);
        emailSenders.add(mockSender2);

        Map<NotificationChannel, Set<NotificationSenderProvider>> senders = Map.of(NotificationChannel.EMAIL, emailSenders);
        useCase = new SendNotificationUseCase(senders);

        var request = SendNotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .subject("Test")
                .body("Hello")
                .build();

        // When
        var result = useCase.send(request);

        // Then
        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.PROVIDER_NOT_FOUND, result.getErrorCode());
        assertEquals("All senders failed", result.getErrorMessage());
    }

    @Test
    void shouldHandleNonValidableSenderSuccessfully() {
        // Given
        var successResult = NotificationResult.success("notif-123", "msg-456", NotificationChannel.EMAIL, "MockSender");

        when(mockSender1.send(any(Notification.class))).thenReturn(successResult);

        Set<NotificationSenderProvider> emailSenders = Set.of(mockSender1);
        Map<NotificationChannel, Set<NotificationSenderProvider>> senders = Map.of(NotificationChannel.EMAIL, emailSenders);
        useCase = new SendNotificationUseCase(senders);

        var request = SendNotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .subject("Test")
                .body("Hello")
                .build();

        // When
        var result = useCase.send(request);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("msg-456", result.getProviderMessageId());
    }

    @Test
    void shouldMapRequestToNotificationCorrectly() {
        // Given
        var successResult = NotificationResult.success("notif-123", "msg-456", NotificationChannel.EMAIL, "MockSender");

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(mockSender1.send(notificationCaptor.capture())).thenReturn(successResult);

        Set<NotificationSenderProvider> emailSenders = Set.of(mockSender1);
        Map<NotificationChannel, Set<NotificationSenderProvider>> senders = Map.of(NotificationChannel.EMAIL, emailSenders);
        useCase = new SendNotificationUseCase(senders);

        var request = SendNotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("test@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .templateId("template-123")
                .priority(Priority.HIGH)
                .from("sender@example.com")
                .replyTo("reply@example.com")
                .build();

        // When
        var result = useCase.send(request);

        // Then
        assertTrue(result.isSuccess());
        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals("test@example.com", capturedNotification.getRecipient());
        assertEquals("Test Subject", capturedNotification.getSubject());
        assertEquals("Test Body", capturedNotification.getBody());
        assertEquals("template-123", capturedNotification.getTemplateId());
        assertEquals(Priority.HIGH, capturedNotification.getPriority());
        assertEquals("sender@example.com", capturedNotification.getFrom());
        assertEquals("reply@example.com", capturedNotification.getReplyTo());
    }
}
