package cc.uncarbon.framework.helium.ratelimiter.constant;

import lombok.experimental.UtilityClass;

/**
 * 限流组件常量
 */
@UtilityClass
public final class RateLimiterConstant {

    /**
     * Redis Key前缀
     */
    public static final String REDIS_KEY_PREFIX = "rate-limiter";

    /**
     * 未知
     */
    public static final String UNKNOWN = "unknown";

}
