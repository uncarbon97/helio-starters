package cc.uncarbon.framework.helium.ratelimiter.annotation;

import cc.uncarbon.framework.helium.ratelimiter.stratrgy.RateLimitStrategy;
import cc.uncarbon.framework.helium.ratelimiter.stratrgy.RateLimitGlobalStrategy;

import java.lang.annotation.*;

/**
 * 标记使用服务端限流，单接口在 duration 秒内只能被请求 max 次
 * 底层实现：Redis + 计数器
 *
 * @author ruoyi
 * @author Uncarbon
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseRateLimit {

    /**
     * 限流标识文本，会拼接至 Redis Key 中
     * 空文本则会使用 Java 方法的全限定名
     */
    String mark() default "";

    /**
     * 限流时长，单位=秒
     */
    int duration() default 60;

    /**
     * 在限流时长内，最多可请求多少次
     */
    int max() default 100;

    /**
     * 限流策略；默认为全局限流
     */
    Class<? extends RateLimitStrategy> strategy() default RateLimitGlobalStrategy.class;
}
