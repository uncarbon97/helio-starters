package cc.uncarbon.framework.helio.ratelimiter.stratrgy;

import cc.uncarbon.framework.helio.ratelimiter.annotation.UseRateLimit;
import cc.uncarbon.framework.helio.ratelimiter.constant.RateLimiterConstant;
import cn.hutool.extra.servlet.JakartaServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 以 IP 为维度限流策略
 * 基于{@link BaseRateLimitRedisStrategy}使用相同 Lua 脚本，仅在 RedisKey 有差异
 *
 * @author Uncarbon
 */
@Slf4j
public class RateLimitByIPStrategy extends BaseRateLimitRedisStrategy implements RateLimitStrategy {

    public RateLimitByIPStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        super(objectRedisTemplate, "[Redis限流器][IP维度]");
    }

    @Override
    public void performRateLimitCheck(UseRateLimit annotation, JoinPoint point) {
        super.performRateLimitCheck(annotation, point, this::rateLimitedExceptionSupplier);
    }

    @Override
    protected String determineRedisKey(UseRateLimit annotation, JoinPoint point) {
        return "%s:ip:%s:%s".formatted(RateLimiterConstant.REDIS_KEY_PREFIX,
                resolveIP(), determineMark(annotation, point));
    }

    /**
     * 获取 IP 地址
     *
     * @return IP 或 {@link RateLimiterConstant#UNKNOWN}
     */
    protected String resolveIP() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes requestAttributes) {
            return JakartaServletUtil.getClientIP(requestAttributes.getRequest());
        }
        // 兜底
        return RateLimiterConstant.UNKNOWN;
    }

}
