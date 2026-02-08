package co.seekglobal.notification.infrastructure.config;

import co.seekglobal.notification.application.inbound.SendNotificationCommand;
import co.seekglobal.notification.application.outbound.NotificationSenderProvider;
import co.seekglobal.notification.application.usecase.SendNotificationUseCase;
import co.seekglobal.notification.domain.NotificationChannel;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SeekNotificationBuilder {

    Map<NotificationChannel, Set<NotificationSenderProvider>> providers = new EnumMap<>(NotificationChannel.class);

    private SeekNotificationBuilder() {
    }

    public static SeekNotificationBuilder builder() {
        return new SeekNotificationBuilder();
    }

    public SeekNotificationBuilder provider(NotificationSenderProvider provider) {
        this.providers.computeIfAbsent(provider.getChannel(), k -> new LinkedHashSet<>()).add(provider);
        return this;
    }

    public SendNotificationCommand build() {
        return new SendNotificationUseCase(this.providers);
    }
}
