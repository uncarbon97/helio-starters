package cc.uncarbon.framework.redis.config;

import cc.uncarbon.framework.redis.lock.RedisDistributedLock;
import cc.uncarbon.framework.redis.lock.impl.RedisDistributedLockImpl;
import cc.uncarbon.framework.redis.support.RedisDistributedLockSupport;
import cc.uncarbon.framework.redis.support.impl.RedisDistributedLockSupportImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * Helio Redis 自动配置类
 *
 * @author zuihou
 * @author Mark sunlightcs@gmail.com
 * @author Uncarbon
 */
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
@RequiredArgsConstructor
@AutoConfiguration
public class HelioRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(factory);

        // 指定相应的序列化方案
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);

        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);

        return redisTemplate;
    }

    /**
     * 缓存键名生成规则
     */
    @Bean
    @ConditionalOnMissingBean
    public KeyGenerator keyGenerator() {
        return (target, method, objects) -> {
            StringBuilder sb = new StringBuilder(64);
            sb.append(target.getClass().getName());
            sb.append(":");
            sb.append(method.getName());
            for (Object obj : objects) {
                if (obj != null) {
                    sb.append(":").append(obj);
                }
            }
            return sb.toString();
        };
    }

    /**
     * 基于 Redisson 的分布式锁
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisDistributedLock redisDistributedLock(RedissonClient redissonClient) {
        return new RedisDistributedLockImpl(redissonClient);
    }

    /**
     * Redis分布式可重入锁辅助类
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisDistributedLockSupport redisDistributedLockSupport(RedisDistributedLock redisDistributedLock) {
        return new RedisDistributedLockSupportImpl(redisDistributedLock);
    }
}
