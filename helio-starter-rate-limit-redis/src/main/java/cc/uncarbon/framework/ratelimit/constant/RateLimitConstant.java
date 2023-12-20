package cc.uncarbon.framework.ratelimit.constant;

/**
 * 限流组件常量
 */
public final class RateLimitConstant {

    private RateLimitConstant() {}

    /**
     * Redis Key前缀
     */
    public static final String REDIS_KEY_PREFIX = "rateLimit:";

    /**
     * 未知客户端IP地址
     */
    public static final String CLIENT_IP_UNKNOWN = "unknown";

}
