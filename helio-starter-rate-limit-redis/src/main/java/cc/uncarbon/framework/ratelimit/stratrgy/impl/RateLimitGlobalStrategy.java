package cc.uncarbon.framework.ratelimit.stratrgy.impl;

import cc.uncarbon.framework.ratelimit.annotation.UseRateLimit;
import cc.uncarbon.framework.ratelimit.constant.RateLimitConstant;
import cc.uncarbon.framework.ratelimit.stratrgy.RateLimitStrategy;
import cn.hutool.core.text.StrPool;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 无维度区分，全局限流策略
 */
@Slf4j
public class RateLimitGlobalStrategy extends SimpleRedisBasedRateLimitStrategy implements RateLimitStrategy {

    public RateLimitGlobalStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        super(objectRedisTemplate, "RateLimitGlobalStrategy");
    }

    @Override
    public void performRateLimitCheck(UseRateLimit annotation, JoinPoint point) {
        super.performRateLimitCheck(annotation, point, this::rateLimitedExceptionSupplier);
    }

    /**
     * 确定RedisKey
     */
    @Override
    protected String determineRedisKey(UseRateLimit annotation, JoinPoint point) {
        final String splitter = StrPool.COLON;
        // 由编译器自动使用StringBuilder拼接，下同
        return RateLimitConstant.REDIS_KEY_PREFIX + "global" + splitter + determineMark(annotation, point);
    }
}
