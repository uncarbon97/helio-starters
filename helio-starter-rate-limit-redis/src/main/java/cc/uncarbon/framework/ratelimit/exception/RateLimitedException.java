package cc.uncarbon.framework.ratelimit.exception;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import cc.uncarbon.framework.core.exception.BusinessException;
import cn.hutool.http.HttpStatus;
import lombok.NonNull;

/**
 * 已被限流异常
 */
public class RateLimitedException extends BusinessException {

    public RateLimitedException() {
        super(HttpStatus.HTTP_TOO_MANY_REQUESTS, "操作频率不要太快");
    }

    public RateLimitedException(String msg) {
        super(msg);
    }

    public RateLimitedException(int code, String msg) {
        super(code, msg);
    }

    public RateLimitedException(int code, String msg, Object... templateParams) {
        super(code, msg, templateParams);
    }

    public RateLimitedException(@NonNull HelioBaseEnum<Integer> customEnum) {
        super(customEnum);
    }

    public RateLimitedException(@NonNull HelioBaseEnum<Integer> customEnum, Object... templateParams) {
        super(customEnum, templateParams);
    }
}
