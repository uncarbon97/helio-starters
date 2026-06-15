package cc.uncarbon.framework.helium.ratelimiter.exception;

import cc.uncarbon.framework.helium.base.errorcode.BuiltinErrorCodeEnum;
import cc.uncarbon.framework.helium.base.exception.BusinessException;

/**
 * 表示「已被限流」的异常
 *
 * @author Uncarbon
 */
public class RateLimitedException extends BusinessException {

    public RateLimitedException() {
        super(BuiltinErrorCodeEnum.Z00429);
    }

    public RateLimitedException(String errorMsg) {
        super(BuiltinErrorCodeEnum.Z00429.getErrorCode(), errorMsg);
    }

    public RateLimitedException(String code, String errorMsg) {
        super(code, errorMsg);
    }

    public RateLimitedException(String code, String errorMsgTemplate, Object... templateParams) {
        super(code, errorMsgTemplate, templateParams);
    }
}
