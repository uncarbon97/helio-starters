package cc.uncarbon.framework.ratelimit.stratrgy.impl;

import cc.uncarbon.framework.ratelimit.annotation.UseRateLimit;
import cc.uncarbon.framework.ratelimit.exception.RateLimitStrategyException;
import cc.uncarbon.framework.ratelimit.exception.RateLimitedException;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * 脚手架预置的简易限流策略基类
 */
@RequiredArgsConstructor
@Slf4j
public abstract class SimpleRedisBasedRateLimitStrategy {

    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final String realClassName;

    /**
     * 预置限流Lua脚本
     */
    private static final String LUA_SCRIPT = "local key = KEYS[1]\n" +
            "local count = tonumber(ARGV[1])\n" +
            "local time = tonumber(ARGV[2])\n" +
            "local current = redis.call('get', key);\n" +
            "if current and tonumber(current) > count then\n" +
            "    return tonumber(current);\n" +
            "end\n" +
            "current = redis.call('incr', key)\n" +
            "if tonumber(current) == 1 then\n" +
            "    redis.call('expire', key, time)\n" +
            "end\n" +
            "return tonumber(current);";

    /**
     * 预置限流Lua脚本的 RedisScript 类实例
     */
    private static final RedisScript<Long> REDIS_SCRIPT = RedisScript.of(LUA_SCRIPT, Long.class);


    protected void performRateLimitCheck(UseRateLimit annotation, JoinPoint point, Supplier<RateLimitedException> rateLimitedExceptionSupplier) throws RateLimitStrategyException {
        long duration = annotation.duration();
        long max = annotation.max();

        String redisKey = redisKeyOf(annotation, point);
        Long current = objectRedisTemplate.execute(REDIS_SCRIPT, Collections.singletonList(redisKey), max, duration);
        if (current == null) {
            throw new RateLimitStrategyException();
        }
        if (current > max) {
            log.info("[UseRateLimit][{}] 键名 {} 已触达限流阈值 {}", realClassName, redisKey, max);
            throw rateLimitedExceptionSupplier.get();
        }
        log.info("[UseRateLimit][{}] 键名 {} 限流进度 {}/{}", realClassName, redisKey, current, max);
    }

    /**
     * 确定RedisKey
     */
    abstract protected String redisKeyOf(UseRateLimit annotation, JoinPoint point);

    /**
     * 确定被限流方法的标识
     */
    protected String markOf(UseRateLimit annotation, JoinPoint point) {
        if (StrUtil.isNotEmpty(annotation.mark())) {
            return annotation.mark();
        }
        // 使用Java方法的全限定名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        return targetClass.getName() + StrPool.DASHED + method.getName();
    }
}
