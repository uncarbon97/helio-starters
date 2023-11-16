package cc.uncarbon.framework.ratelimit.stratrgy;

import cc.uncarbon.framework.core.context.UserContextHolder;
import cc.uncarbon.framework.ratelimit.annotation.UseRateLimit;
import cc.uncarbon.framework.ratelimit.constant.RateLimitConstant;
import cc.uncarbon.framework.ratelimit.exception.RateLimitedException;
import cc.uncarbon.framework.web.util.IPUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.aspectj.lang.JoinPoint;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigInteger;

/**
 * 限流策略接口
 */
public interface RateLimitStrategy {

    /**
     * 取当前用户ID
     */
    default Long currentUserId() {
        // 默认值为0
        return ObjectUtil.defaultIfNull(UserContextHolder.getUserId(), BigInteger.ZERO::longValue);
    }

    /**
     * 取当前客户端IP
     */
    default String currentClientIP() {
        String clientIP = UserContextHolder.getClientIP();
        if (StrUtil.isNotEmpty(clientIP)) {
            return clientIP;
        }

        // 尝试从request中得到未登录用户的IP地址
        try {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes != null) {
                return IPUtil.getClientIPAddress(servletRequestAttributes.getRequest(), 0);
            }
        } catch (Exception | NoClassDefFoundError e) {
            // 可能没有引入 helio-starter-web 依赖，忽略
        }

        // 兜底
        return RateLimitConstant.CLIENT_IP_UNKNOWN;
    }

    /**
     * 限流异常生产者
     * 通过覆写本方法，可替换提示消息、实现国际化文案等
     */
    default RateLimitedException rateLimitedExceptionSupplier() {
        return new RateLimitedException();
    }

    /**
     * 执行限流检查，根据不同维度判断是否已触达限流上限
     * @param annotation 注解实例
     * @param point 切点
     */
    void performRateLimitCheck(UseRateLimit annotation, JoinPoint point);

}
