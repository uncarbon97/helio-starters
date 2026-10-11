package cc.uncarbon.framework.helium.ratelimiter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Redis 不可用时的限流降级策略
 *
 * @author Uncarbon
 */
@AllArgsConstructor
@Getter
public enum RateLimitDegradationEnum {

    /**
     * fail-closed（默认）：抛出语义化异常拒绝请求，保持限流保护语义，业务方收到明确错误
     */
    FAIL_CLOSED,

    /**
     * fail-open：放行请求并打印 error 告警日志，优先保业务可用性
     */
    FAIL_OPEN,
}
