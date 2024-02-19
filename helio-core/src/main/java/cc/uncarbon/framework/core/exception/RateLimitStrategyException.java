package cc.uncarbon.framework.core.exception;

/**
 * 限流策略异常
 * 用于helio-starter-rate-limit-redis
 */
public class RateLimitStrategyException extends HelioFrameworkException {

    public RateLimitStrategyException(String msg) {
        super(msg);
    }
}
