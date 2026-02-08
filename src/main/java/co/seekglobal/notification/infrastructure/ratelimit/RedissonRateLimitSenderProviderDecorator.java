package co.seekglobal.notification.infrastructure.ratelimit;

import co.seekglobal.notification.application.outbound.NotificationSenderProvider;
import co.seekglobal.notification.domain.Notification;
import co.seekglobal.notification.domain.NotificationChannel;
import co.seekglobal.notification.domain.NotificationResult;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

@Slf4j
public final class RedissonRateLimitSenderProviderDecorator implements NotificationSenderProvider {

    private final NotificationSenderProvider delegate;
    private final DistributedRateLimitConfig config;
    private final RRateLimiter rRateLimiter;

    @Builder(builderMethodName = "factory")
    public RedissonRateLimitSenderProviderDecorator(NotificationSenderProvider delegate,
                                                    DistributedRateLimitConfig config,
                                                    RedissonClient redissonClient) {
        this.delegate = delegate;
        this.config = config;
        this.rRateLimiter = redissonClient.getRateLimiter(String.join("_", delegate.getProviderName(), "rate_limiter"));
        rRateLimiter.trySetRate(RateType.OVERALL, config.getMaxRequestsPerWindow(), 1, RateIntervalUnit.SECONDS);
        log.info("[DistributedRateLimit] Initialized for provider {} with {} req/{} and rate {}",
                delegate.getProviderName(),
                config.getMaxRequestsPerWindow(),
                config.getRateIntervalUnit(),
                config.getRate());
    }

    @Override
    public NotificationResult send(Notification notification) {
        rRateLimiter.acquire();
        return delegate.send(notification);
    }

    @Override
    public NotificationChannel getChannel() {
        return delegate.getChannel();
    }

    @Override
    public String getProviderName() {
        return delegate.getProviderName() + "[DistributedRateLimited]";
    }
}
