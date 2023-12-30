package cc.uncarbon.framework.ratelimit.exception;

import cc.uncarbon.framework.core.exception.HelioFrameworkException;

/**
 * 限流策略异常
 */
public class RateLimitStrategyException extends HelioFrameworkException {

    public RateLimitStrategyException(String msg) {
        super(msg);
    }
}
