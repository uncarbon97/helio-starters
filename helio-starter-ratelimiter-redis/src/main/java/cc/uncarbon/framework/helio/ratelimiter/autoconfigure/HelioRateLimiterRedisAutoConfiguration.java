package cc.uncarbon.framework.helio.ratelimiter.autoconfigure;


import cc.uncarbon.framework.helio.ratelimiter.aop.UseRateLimitAspect;
import cc.uncarbon.framework.helio.ratelimiter.stratrgy.RateLimitByIPStrategy;
import cc.uncarbon.framework.helio.ratelimiter.stratrgy.RateLimitByUserStrategy;
import cc.uncarbon.framework.helio.ratelimiter.stratrgy.RateLimitGlobalStrategy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Helio 基于 Redis 的限流器自动配置类
 *
 * @author Uncarbon
 */
@AutoConfiguration
public class HelioRateLimiterRedisAutoConfiguration {

    /**
     * 切面
     */
    @Bean
    @ConditionalOnMissingBean
    public UseRateLimitAspect useRateLimitAspect() {
        return new UseRateLimitAspect();
    }

    /**
     * 注册到 Spring Bean 容器中，方便复用单例
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimitGlobalStrategy rateLimitGlobalStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        return new RateLimitGlobalStrategy(objectRedisTemplate);
    }

    /**
     * 注册到 Spring Bean 容器中，方便复用单例
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimitByUserStrategy rateLimitByUserStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        return new RateLimitByUserStrategy(objectRedisTemplate);
    }

    /**
     * 注册到 Spring Bean 容器中，方便复用单例
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimitByIPStrategy rateLimitByIPStrategy(RedisTemplate<String, Object> objectRedisTemplate) {
        return new RateLimitByIPStrategy(objectRedisTemplate);
    }

}
