package cc.uncarbon.framework.helium.ratelimiter.stratrgy;

import cc.uncarbon.framework.helium.ratelimiter.annotation.UseRateLimit;
import cc.uncarbon.framework.helium.ratelimiter.constant.RateLimiterConstant;
import cc.uncarbon.framework.helium.ratelimiter.enums.RateLimitDegradationEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 以 IP 为维度限流策略
 * 基于{@link BaseRateLimitRedisStrategy}使用相同 Lua 脚本，仅在 RedisKey 有差异
 * <p>IP 上下文缺失（MQ/定时任务等无请求上下文）时跳过限流，不再归并到 unknown 大桶。</p>
 *
 * @author Uncarbon
 */
@Slf4j
public class RateLimitByIPStrategy extends BaseRateLimitRedisStrategy implements RateLimitStrategy {

    public RateLimitByIPStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        super(objectRedisTemplate, "[Redis限流器][IP维度]");
    }

    public RateLimitByIPStrategy(RedisTemplate<String, Object> objectRedisTemplate,
                                 RateLimitDegradationEnum degradation) {
        super(objectRedisTemplate, "[Redis限流器][IP维度]", degradation);
    }

    @Override
    public void performRateLimitCheck(UseRateLimit annotation, JoinPoint point) {
        super.performRateLimitCheck(annotation, point, this::rateLimitedExceptionSupplier);
    }

    @Override
    protected String determineRedisKey(UseRateLimit annotation, JoinPoint point) {
        String ip = resolveClientIpOrNull();
        if (ip == null) {
            // 维度彻底缺失：返回 null 由基类放行 + warn
            return null;
        }
        return "%s:ip:%s:%s".formatted(RateLimiterConstant.REDIS_KEY_PREFIX,
                ip, determineMark(annotation, point));
    }

}
