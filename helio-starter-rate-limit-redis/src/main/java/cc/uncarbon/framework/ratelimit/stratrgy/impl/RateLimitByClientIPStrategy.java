package cc.uncarbon.framework.ratelimit.stratrgy.impl;

import cc.uncarbon.framework.ratelimit.annotation.UseRateLimit;
import cc.uncarbon.framework.ratelimit.constant.RateLimitConstant;
import cc.uncarbon.framework.ratelimit.stratrgy.RateLimitStrategy;
import cn.hutool.core.text.StrPool;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 以客户端IP为维度限流策略
 * 基于{@link SimpleRedisBasedRateLimitStrategy}使用相同Lua脚本，仅在RedisKey有差异
 */
@Slf4j
public class RateLimitByClientIPStrategy extends SimpleRedisBasedRateLimitStrategy implements RateLimitStrategy {

    public RateLimitByClientIPStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        super(objectRedisTemplate, "RateLimitByClientIPStrategy");
    }

    @Override
    public void performRateLimitCheck(UseRateLimit annotation, JoinPoint point) {
        super.performRateLimitCheck(annotation, point, this::rateLimitedExceptionSupplier);
    }

    @Override
    protected String redisKeyOf(UseRateLimit annotation, JoinPoint point) {
        final String splitter = StrPool.COLON;
        return RateLimitConstant.REDIS_KEY_PREFIX + "ip" + splitter + currentClientIP() + splitter + markOf(annotation, point);
    }

}
