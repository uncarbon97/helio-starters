package cc.uncarbon.framework.ratelimit.constant;

/**
 * 限流组件常量
 */
public interface RateLimitConstant {

    /**
     * Redis Key前缀
     */
    String REDIS_KEY_PREFIX = "rateLimit:";

    /**
     * 未知客户端IP地址
     */
    String CLIENT_IP_UNKNOWN = "unknown";

    /**
     * IPUtil的全限定名
     */
    String IP_UTIL_FULL_CLASS_NAME = "cc.uncarbon.framework.web.util.IPUtil";
}
