package cc.uncarbon.framework.helium.ratelimiter.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.ratelimiter.enums.RateLimitDegradationEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helium 基于 Redis 的限流器配置属性类
 *
 * @author Uncarbon
 */
@Data
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.RATE_LIMITER)
public class HeliumRateLimitProperties {

    /**
     * Redis 不可用时的降级策略
     */
    private RateLimitDegradationEnum degradation = RateLimitDegradationEnum.FAIL_CLOSED;

}
