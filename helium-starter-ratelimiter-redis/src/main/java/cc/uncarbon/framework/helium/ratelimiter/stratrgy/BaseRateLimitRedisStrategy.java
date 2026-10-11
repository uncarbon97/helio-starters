package cc.uncarbon.framework.helium.ratelimiter.stratrgy;

import cc.uncarbon.framework.helium.ratelimiter.annotation.UseRateLimit;
import cc.uncarbon.framework.helium.ratelimiter.enums.RateLimitDegradationEnum;
import cc.uncarbon.framework.helium.ratelimiter.exception.RateLimitedException;
import cc.uncarbon.framework.helium.web.context.VisitorContext;
import cc.uncarbon.framework.helium.web.context.VisitorContextHolder;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.dao.DataAccessException;
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
     * Redis 不可用时的降级策略；默认 fail-closed 抛语义化异常
     */
    private final RateLimitDegradationEnum degradation;


    protected BaseRateLimitRedisStrategy(RedisTemplate<String, Object> objectRedisTemplate, String logPrefix) {
        this(objectRedisTemplate, logPrefix, RateLimitDegradationEnum.FAIL_CLOSED);
    }

    protected BaseRateLimitRedisStrategy(RedisTemplate<String, Object> objectRedisTemplate, String logPrefix,
                                         RateLimitDegradationEnum degradation) {
        this.objectRedisTemplate = objectRedisTemplate;
        this.LOG_PREFIX = logPrefix;
        this.degradation = degradation;
    }

    /**
     * 预置限流Lua脚本的 RedisScript 类实例
     */
    private static final RedisScript<Long> REDIS_SCRIPT = RedisScript.of(LUA_SCRIPT, Long.class);


    protected void performRateLimitCheck(UseRateLimit annotation, JoinPoint point,
                                         Supplier<RateLimitedException> rateLimitedExceptionSupplier) {
        int duration = annotation.duration();
        int max = annotation.max();
        // 前置校验：duration 非法会在 Lua 层报 invalid expire time，max 非法则限流永不过期/永远触发
        if (duration <= 0 || max <= 0) {
            throw new IllegalArgumentException(
                    "@UseRateLimit 配置非法：duration 与 max 必须为正数（当前 duration=" + duration + ", max=" + max + "）");
        }

        String redisKey = determineRedisKey(annotation, point);
        if (redisKey == null) {
            // 维度彻底缺失（MQ/定时任务等无请求上下文）：无法归因即不计数，放行，避免归并大桶互相误伤
            log.warn("{} 限流维度缺失，跳过本次限流 >> method={}", LOG_PREFIX, point.getSignature().toShortString());
            return;
        }

        Long current;
        try {
            current = objectRedisTemplate.execute(REDIS_SCRIPT, List.of(redisKey), max, duration);
        } catch (DataAccessException e) {
            if (degradation == RateLimitDegradationEnum.FAIL_OPEN) {
                log.error("{} Redis 不可用，fail-open 放行 >> method={}", LOG_PREFIX,
                        point.getSignature().toShortString(), e);
                return;
            }
            // fail-closed：抛语义化异常
            throw new IllegalStateException("限流组件不可用（Redis 访问失败），fail-closed 模式拒绝请求", e);
        }
        if (current == null) {
            log.error("{} SCRIPT 已执行但无返回结果", LOG_PREFIX);
            throw new RuntimeException("SCRIPT 已执行但无返回结果");
        }
        if (current > max) {
            log.info("{} 键名 {} 已触达限流阈值 {}", LOG_PREFIX, redisKey, max);
            throw rateLimitedExceptionSupplier.get();
        }
        log.info("{} 键名 {} 限流进度 {}/{}", LOG_PREFIX, redisKey, current, max);
    }

    /**
     * 确定RedisKey；维度彻底缺失时返回 {@code null}（基类放行 + warn）
     */
    protected abstract String determineRedisKey(UseRateLimit annotation, JoinPoint point);

    /**
     * 确定被限流方法的标识
     */
    protected String determineMark(UseRateLimit annotation, JoinPoint point) {
        if (CharSequenceUtil.isNotEmpty(annotation.mark())) {
            return annotation.mark();
        }
        // 兜底：使用 Java 方法的全限定名；同名重载方法共享限流桶（重载通常是同一业务）
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        return targetClass.getName() + StrPool.DASHED + method.getName();
    }

    /**
     * 解析当前访客 IP（单值）；无请求上下文时返回 {@code null}
     */
    protected String resolveClientIpOrNull() {
        return VisitorContextHolder.getContextOptional()
                .map(VisitorContext::getClientIp)
                .filter(CharSequenceUtil::isNotBlank)
                .orElse(null);
    }
}
