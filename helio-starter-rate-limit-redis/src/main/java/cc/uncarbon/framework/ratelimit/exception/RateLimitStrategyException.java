package cc.uncarbon.framework.ratelimit.exception;

import cc.uncarbon.framework.core.exception.BusinessException;
import cn.hutool.http.HttpStatus;

/**
 * 限流策略异常
 * 一般是【无法得到限流策略实例】
 */
public class RateLimitStrategyException extends BusinessException {

    public RateLimitStrategyException() {
        super(HttpStatus.HTTP_INTERNAL_ERROR, "限流策略发生内部错误");
    }
}
