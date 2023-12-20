package cc.uncarbon.framework.ratelimit.stratrgy.impl;

import cc.uncarbon.framework.core.context.UserContextHolder;
import cc.uncarbon.framework.ratelimit.annotation.UseRateLimit;
import cc.uncarbon.framework.ratelimit.constant.RateLimitConstant;
import cc.uncarbon.framework.ratelimit.stratrgy.RateLimitStrategy;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigInteger;

/**
 * 以当前用户ID为维度限流策略
 * 基于{@link SimpleRedisBasedRateLimitStrategy}使用相同Lua脚本，仅在RedisKey有差异
 */
@Slf4j
public class RateLimitByUserIdStrategy extends SimpleRedisBasedRateLimitStrategy implements RateLimitStrategy {

    public RateLimitByUserIdStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        super(objectRedisTemplate, "RateLimitByUserIdStrategy");
    }

    @Override
    public void performRateLimitCheck(UseRateLimit annotation, JoinPoint point) {
        super.performRateLimitCheck(annotation, point, this::rateLimitedExceptionSupplier);
    }

    @Override
    protected String determineRedisKey(UseRateLimit annotation, JoinPoint point) {
        final String splitter = StrPool.COLON;
        return RateLimitConstant.REDIS_KEY_PREFIX + "userId" + splitter + currentUserId() + splitter + determineMark(annotation, point);
    }

    /**
     * 取当前用户ID
     */
    protected Long currentUserId() {
        // 默认值=0
        return ObjectUtil.defaultIfNull(UserContextHolder.getUserId(), BigInteger.ZERO::longValue);
    }
}
