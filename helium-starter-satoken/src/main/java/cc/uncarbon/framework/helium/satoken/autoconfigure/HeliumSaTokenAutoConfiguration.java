package cc.uncarbon.framework.helium.satoken.autoconfigure;

import cc.uncarbon.framework.helium.satoken.dao.SaTokenRedisDaoWithLocalCache;
import cc.uncarbon.framework.helium.satoken.props.HeliumSaTokenProperties;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

/**
 * Helium 集成 SA-Token 自动装配类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HeliumSaTokenProperties.class})
@AutoConfiguration
public class HeliumSaTokenAutoConfiguration {

    /**
     * 带本地缓存的 {@link SaTokenDaoForRedisTemplate}
     */
    @Conditional(value = OnLocalCacheDaoEnabled.class)
    @ConditionalOnMissingBean(value = SaTokenDao.class)
    @Primary
    @Bean
    public SaTokenDao saTokenLocalCacheDao(HeliumSaTokenProperties props) {
        var subProp = props.getRedisDaoWithLocalCache();
        return new SaTokenRedisDaoWithLocalCache(subProp.getDuration(), subProp.getCapacity());
    }

    private static class OnLocalCacheDaoEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = Objects.requireNonNull(context.getBeanFactory()).getBean(HeliumSaTokenProperties.class);
            var subProp = props.getRedisDaoWithLocalCache();
            return Boolean.TRUE.equals(subProp.getEnabled());
        }
    }

}
