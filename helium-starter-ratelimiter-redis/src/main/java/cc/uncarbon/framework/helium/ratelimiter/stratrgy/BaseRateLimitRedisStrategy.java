package cc.uncarbon.framework.helium.ratelimiter.stratrgy;

import cc.uncarbon.framework.helium.ratelimiter.annotation.UseRateLimit;
import cc.uncarbon.framework.helium.ratelimiter.exception.RateLimitedException;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

/**
 * 脚手架预置的简易限流策略基类
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public abstract class BaseRateLimitRedisStrategy {

    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final String LOG_PREFIX;

    /**
     * 预置限流Lua脚本
     */
    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local count = tonumber(ARGV[1])
            local time = tonumber(ARGV[2])
            local current = redis.call('get', key)
            
            if current then
                current = tonumber(current)
                if current > count then
                    return current
                end
                current = redis.call('incr', key)
            else
                current = 1
                redis.call('set', key, current, 'EX', time)
            end
            
            return current
            """;

    /**
     * 预置限流Lua脚本的 RedisScript 类实例
     */
    private static final RedisScript<Long> REDIS_SCRIPT = RedisScript.of(LUA_SCRIPT, Long.class);


    protected void performRateLimitCheck(UseRateLimit annotation, JoinPoint point,
                                         Supplier<RateLimitedException> rateLimitedExceptionSupplier) {
        int duration = annotation.duration();
        int max = annotation.max();

        String redisKey = determineRedisKey(annotation, point);
        Long current = objectRedisTemplate.execute(REDIS_SCRIPT, List.of(redisKey), max, duration);
        if (current == null) {
            log.error("{} REDIS_SCRIPT executed but no result.", LOG_PREFIX);
            throw new RuntimeException("SCRIPT executed but no result");
        }
        if (current > max) {
            log.info("{} 键名 {} 已触达限流阈值 {}", LOG_PREFIX, redisKey, max);
            throw rateLimitedExceptionSupplier.get();
        }
        log.info("{} 键名 {} 限流进度 {}/{}", LOG_PREFIX, redisKey, current, max);
    }

    /**
     * 确定RedisKey
     */
    protected abstract String determineRedisKey(UseRateLimit annotation, JoinPoint point);

    /**
     * 确定被限流方法的标识
     */
    protected String determineMark(UseRateLimit annotation, JoinPoint point) {
        if (CharSequenceUtil.isNotEmpty(annotation.mark())) {
            return annotation.mark();
        }
        // 使用 Java 方法的全限定名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        return targetClass.getName() + StrPool.DASHED + method.getName();
    }
}
