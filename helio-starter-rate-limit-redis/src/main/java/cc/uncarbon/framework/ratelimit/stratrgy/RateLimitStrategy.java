package cc.uncarbon.framework.ratelimit.stratrgy;

import cc.uncarbon.framework.ratelimit.annotation.UseRateLimit;
import cc.uncarbon.framework.ratelimit.exception.RateLimitedException;
import org.aspectj.lang.JoinPoint;

/**
 * 限流策略接口
 */
public interface RateLimitStrategy {

    /**
     * 限流异常生产者
     * 通过覆写本方法，可替换提示消息、实现国际化文案等
     */
    default RateLimitedException rateLimitedExceptionSupplier() {
        return new RateLimitedException();
    }

    /**
     * 执行限流检查，根据不同维度判断是否已触达限流上限
     * @param annotation 注解实例
     * @param point 切点
     */
    void performRateLimitCheck(UseRateLimit annotation, JoinPoint point);

}
