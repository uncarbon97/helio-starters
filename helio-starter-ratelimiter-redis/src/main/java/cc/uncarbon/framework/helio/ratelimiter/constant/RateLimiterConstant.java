package cc.uncarbon.framework.helio.ratelimiter.constant;

/**
 * 限流组件常量
 */
public final class RateLimiterConstant {

    private RateLimiterConstant() {
    }

    /**
     * Redis Key前缀
     */
    public static final String REDIS_KEY_PREFIX = "rate-limiter";

    /**
     * 未知
     */
    public static final String UNKNOWN = "unknown";

}
