package cc.uncarbon.framework.ratelimit.stratrgy.impl;

import cc.uncarbon.framework.core.context.UserContextHolder;
import cc.uncarbon.framework.ratelimit.annotation.UseRateLimit;
import cc.uncarbon.framework.ratelimit.constant.RateLimitConstant;
import cc.uncarbon.framework.ratelimit.stratrgy.RateLimitStrategy;
import cc.uncarbon.framework.web.util.IPUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    protected String determineRedisKey(UseRateLimit annotation, JoinPoint point) {
        final String splitter = StrPool.COLON;
        // 编译器将自动优化
        return RateLimitConstant.REDIS_KEY_PREFIX
                + "ip" + splitter + currentClientIP() + splitter + determineMark(annotation, point);
    }

    /**
     * 取当前客户端IP
     */
    protected String currentClientIP() {
        String clientIP = UserContextHolder.getClientIP();
        if (CharSequenceUtil.isNotEmpty(clientIP)) {
            return clientIP;
        }

        // 尝试从request中得到未登录用户的IP地址
        try {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes != null) {
                return IPUtil.getClientIPAddress(servletRequestAttributes.getRequest(), 0);
            }
        } catch (Exception | NoClassDefFoundError e) {
            // 可能没有引入 helio-starter-web 依赖，忽略
        }

        // 兜底
        return RateLimitConstant.CLIENT_IP_UNKNOWN;
    }

}
