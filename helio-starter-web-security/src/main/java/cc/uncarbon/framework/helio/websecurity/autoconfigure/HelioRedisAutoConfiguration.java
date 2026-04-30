package cc.uncarbon.framework.helio.websecurity.autoconfigure;

import cc.uncarbon.framework.helio.jackson.factory.JsonMapperFactory;
import cc.uncarbon.framework.helio.jackson.props.BaseEnumConfig;
import cc.uncarbon.framework.helio.websecurity.lock.RedisDistributedLock;
import cc.uncarbon.framework.helio.websecurity.lock.RedisDistributedLockTemplate;
import cc.uncarbon.framework.helio.websecurity.lock.impl.RedisDistributedLockImpl;
import cc.uncarbon.framework.helio.websecurity.lock.impl.RedisDistributedLockTemplateImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
 * Helio 集成 Redis 自动配置类
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
@AutoConfiguration
public class HelioRedisAutoConfiguration {

}
