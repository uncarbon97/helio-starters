package cc.uncarbon.framework.redis.config;

import cc.uncarbon.framework.redis.lock.RedisDistributedLock;
import cc.uncarbon.framework.redis.lock.impl.RedisDistributedLockImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * Redis 配置类
 *
 * @author zuihou
 * @author Mark sunlightcs@gmail.com
 */
@RequiredArgsConstructor
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
@Configuration
public class RedisAutoConfiguration {

    private final RedisConnectionFactory factory;

    private final RedissonClient redissonClient;


    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        /*
        还有一种StringRedisSerializer序列化器
         */
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(factory);

        return redisTemplate;
    }

    /**
     * 基于Redisson的分布式锁
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisDistributedLock redisDistributedLock() {
        return new RedisDistributedLockImpl(redissonClient);
    }

    /**
     * Key名生成规则
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, objects) -> {
            StringBuilder sb = new StringBuilder();
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
}
