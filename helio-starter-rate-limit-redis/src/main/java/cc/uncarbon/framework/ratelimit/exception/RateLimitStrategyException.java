package cc.uncarbon.framework.ratelimit.exception;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import cc.uncarbon.framework.core.exception.BusinessException;
import cn.hutool.http.HttpStatus;
import lombok.NonNull;

/**
 * 限流策略异常
 */
public class RateLimitStrategyException extends BusinessException {

    public RateLimitStrategyException() {
        super(HttpStatus.HTTP_INTERNAL_ERROR, "限流策略发生内部错误");
    }

    public RateLimitStrategyException(String msg) {
        super(HttpStatus.HTTP_INTERNAL_ERROR, msg);
    }

    public RateLimitStrategyException(int code, String msg) {
        super(code, msg);
    }

    public RateLimitStrategyException(int code, String msg, Object... templateParams) {
        super(code, msg, templateParams);
    }

    public RateLimitStrategyException(@NonNull HelioBaseEnum<Integer> customEnum) {
        super(customEnum);
    }

    public RateLimitStrategyException(@NonNull HelioBaseEnum<Integer> customEnum, Object... templateParams) {
        super(customEnum, templateParams);
    }

}
