package cc.uncarbon.framework.helium.redis.autoconfigure;

import cc.uncarbon.framework.helium.jackson.factory.JsonMapperFactory;
import cc.uncarbon.framework.helium.jackson.props.BaseEnumConfig;
import cc.uncarbon.framework.helium.redis.lock.RedisDistributedLock;
import cc.uncarbon.framework.helium.redis.lock.RedisDistributedLockTemplate;
import cc.uncarbon.framework.helium.redis.lock.impl.RedisDistributedLockImpl;
import cc.uncarbon.framework.helium.redis.lock.impl.RedisDistributedLockTemplateImpl;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfigurationV4;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Helium 集成 Redis 自动装配类
 *
 * @author Uncarbon
 */
@AutoConfigureBefore(value = {DataRedisAutoConfiguration.class, RedissonAutoConfigurationV4.class})
@AutoConfiguration
public class HeliumRedisAutoConfiguration {

    /**
     * 首选 {@link RedisTemplate}，支持各类 K、V 类型
     */
    @Bean
    public RedisTemplate<?, ?> redisTemplate(final RedisConnectionFactory factory) {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        // Redis 中保存的 BaseEnum 类型字段，默认不附加 Label 字段
        BaseEnumConfig baseEnumConfig = new BaseEnumConfig();
        baseEnumConfig.setShowLabel(false);
        JsonMapper jsonMapperForRedis = JsonMapperFactory.enhacedJsonMapper(
                Locale.getDefault(), TimeZone.getTimeZone(ZoneId.systemDefault()),
                // Redis 中保存的大整数，不必转换成字符串
                false, baseEnumConfig);

        // 指定相应的序列化方案
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJacksonJsonRedisSerializer valueSerializer = new GenericJacksonJsonRedisSerializer(jsonMapperForRedis);

        // 键名序列化
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);

        // 键值序列化
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
    public RedisDistributedLock redisDistributedLock(final RedissonClient redissonClient) {
        return new RedisDistributedLockImpl(redissonClient);
    }

    /**
     * 分布式锁模板
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisDistributedLockTemplate redisDistributedLockTemplate(final RedisDistributedLock redisDistributedLock) {
        return new RedisDistributedLockTemplateImpl(redisDistributedLock);
    }
}
