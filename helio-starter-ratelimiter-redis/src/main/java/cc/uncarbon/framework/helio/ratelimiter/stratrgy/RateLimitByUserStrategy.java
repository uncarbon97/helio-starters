package cc.uncarbon.framework.helio.ratelimiter.stratrgy;

import cc.uncarbon.framework.helio.base.context.UserContextHolder;
import cc.uncarbon.framework.helio.ratelimiter.annotation.UseRateLimit;
import cc.uncarbon.framework.helio.ratelimiter.constant.RateLimiterConstant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

/**
 * 以用户为维度限流策略
 * 基于{@link BaseRateLimitRedisStrategy}使用相同 Lua 脚本，仅在 RedisKey 有差异
 *
 * @author Uncarbon
 */
@Slf4j
public class RateLimitByUserStrategy extends BaseRateLimitRedisStrategy implements RateLimitStrategy {

    public RateLimitByUserStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        super(objectRedisTemplate, "[Redis限流器][用户维度]");
    }

    @Override
    public void performRateLimitCheck(UseRateLimit annotation, JoinPoint point) {
        super.performRateLimitCheck(annotation, point, this::rateLimitedExceptionSupplier);
    }

    @Override
    protected String determineRedisKey(UseRateLimit annotation, JoinPoint point) {
        return "%s:user:%s:%s".formatted(RateLimiterConstant.REDIS_KEY_PREFIX,
                resolveUser(), determineMark(annotation, point));
    }

    /**
     * 获取用户标识
     *
     * @return 用户标识 或 {@link RateLimiterConstant#UNKNOWN}
     */
    protected String resolveUser() {
        Long userId = UserContextHolder.getUserId();
        if (Objects.nonNull(userId)) {
            return String.valueOf(userId);
        }
        return RateLimiterConstant.UNKNOWN;
    }
}
