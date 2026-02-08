package co.seekglobal.notification.application.outbound;

import co.seekglobal.notification.domain.Notification;

import java.util.List;

public interface ValidableSender {

    List<ValidationError> validate(Notification notification);

    record ValidationError(String field, String message, String code) {

        public static ValidationError of(String field, String message) {
            return new ValidationError(field, message, field.toUpperCase());
        }

        public static ValidationError of(String field, String message, String code) {
            return new ValidationError(field, message, code);
        }
    }
}
