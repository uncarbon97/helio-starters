package cc.uncarbon.framework.helium.ratelimiter.stratrgy;

import cc.uncarbon.framework.helium.base.context.UserContextHolder;
import cc.uncarbon.framework.helium.ratelimiter.annotation.UseRateLimit;
import cc.uncarbon.framework.helium.ratelimiter.constant.RateLimiterConstant;
import cc.uncarbon.framework.helium.ratelimiter.enums.RateLimitDegradationEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

/**
 * 以用户为维度限流策略
 * 基于{@link BaseRateLimitRedisStrategy}使用相同 Lua 脚本，仅在 RedisKey 有差异
 * <p>未登录请求降级到 IP 维度（带降级标记，不与 IP 维度策略原生桶互相挤占）；
 * IP 上下文也缺失（MQ/定时任务等）时跳过限流。</p>
 *
 * @author Uncarbon
 */
@Slf4j
public class RateLimitByUserStrategy extends BaseRateLimitRedisStrategy implements RateLimitStrategy {

    public RateLimitByUserStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        super(objectRedisTemplate, "[Redis限流器][用户维度]");
    }

    public RateLimitByUserStrategy(RedisTemplate<String, Object> objectRedisTemplate,
                                   RateLimitDegradationEnum degradation) {
        super(objectRedisTemplate, "[Redis限流器][用户维度]", degradation);
    }

    @Override
    public void performRateLimitCheck(UseRateLimit annotation, JoinPoint point) {
        super.performRateLimitCheck(annotation, point, this::rateLimitedExceptionSupplier);
    }

    @Override
    protected String determineRedisKey(UseRateLimit annotation, JoinPoint point) {
        String userDimension = resolveUserDimension();
        if (userDimension == null) {
            // 维度彻底缺失：返回 null 由基类放行 + warn
            return null;
        }
        return "%s:user:%s:%s".formatted(RateLimiterConstant.REDIS_KEY_PREFIX,
                userDimension, determineMark(annotation, point));
    }

    /**
     * 解析用户维度标识
     *
     * @return 已登录：用户 ID；未登录：降级 IP 维度（anonymous@ip 前缀标记）；
     *         IP 上下文也缺失（MQ/定时任务等无请求上下文）返回 {@code null}
     */
    protected String resolveUserDimension() {
        Long userId = UserContextHolder.getUserId();
        if (Objects.nonNull(userId)) {
            return String.valueOf(userId);
        }
        String ip = resolveClientIpOrNull();
        return ip == null ? null : "anonymous@" + ip;
    }
}
