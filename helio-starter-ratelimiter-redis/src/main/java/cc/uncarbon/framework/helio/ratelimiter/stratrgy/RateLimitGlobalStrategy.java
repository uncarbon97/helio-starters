package cc.uncarbon.framework.helio.ratelimiter.stratrgy;

import cc.uncarbon.framework.helio.ratelimiter.annotation.UseRateLimit;
import cc.uncarbon.framework.helio.ratelimiter.constant.RateLimiterConstant;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 无维度区分，全局限流策略
 *
 * @author Uncarbon
 */
@Slf4j
public class RateLimitGlobalStrategy extends BaseRateLimitRedisStrategy implements RateLimitStrategy {

    public RateLimitGlobalStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        super(objectRedisTemplate, "[Redis限流器][全局维度]");
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
        return "%s:global:%s".formatted(RateLimiterConstant.REDIS_KEY_PREFIX, determineMark(annotation, point));
    }
}
