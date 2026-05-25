package cc.uncarbon.framework.helium.ratelimiter.exception;

import cc.uncarbon.framework.helium.base.exception.BusinessException;
import cn.hutool.http.HttpStatus;

/**
 * 表示「已被限流」的异常
 *
 * @author Uncarbon
 */
public class RateLimitedException extends BusinessException {

    public RateLimitedException() {
        super(HttpStatus.HTTP_TOO_MANY_REQUESTS, "操作频率不要太快，稍微休息一下吧");
    }

    public RateLimitedException(String errorMsg) {
        super(HttpStatus.HTTP_TOO_MANY_REQUESTS, errorMsg);
    }

    public RateLimitedException(int code, String errorMsg) {
        super(code, errorMsg);
    }

    public RateLimitedException(String code, String errorMsg) {
        super(code, errorMsg);
    }

    public RateLimitedException(int code, String errorMsgTemplate, Object... templateParams) {
        super(code, errorMsgTemplate, templateParams);
    }

    public RateLimitedException(String code, String errorMsgTemplate, Object... templateParams) {
        super(code, errorMsgTemplate, templateParams);
    }
}
