package co.seekglobal.notification.infrastructure.ratelimit;

import lombok.Builder;
import lombok.Getter;
import org.redisson.api.RateIntervalUnit;

@Getter
@Builder
public class DistributedRateLimitConfig {

    @Builder.Default
    private final int maxRequestsPerWindow = 10;

    @Builder.Default
    private final RateIntervalUnit rateIntervalUnit = RateIntervalUnit.SECONDS;

    @Builder.Default
    private final int rate = 1;

    public static DistributedRateLimitConfig of(int maxRequestsPerWindow, int rate, RateIntervalUnit unit) {
        return DistributedRateLimitConfig.builder()
                .maxRequestsPerWindow(maxRequestsPerWindow)
                .rate(rate)
                .rateIntervalUnit(unit)
                .build();
    }
}
