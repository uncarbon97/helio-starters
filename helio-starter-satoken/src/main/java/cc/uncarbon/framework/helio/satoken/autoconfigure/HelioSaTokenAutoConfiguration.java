package cc.uncarbon.framework.helio.satoken.autoconfigure;

import cc.uncarbon.framework.helio.satoken.dao.SaTokenRedisDaoWithLocalCache;
import cc.uncarbon.framework.helio.satoken.props.HelioSaTokenProperties;
import cn.dev33.satoken.dao.SaTokenDao;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

/**
 * Helio 集成 SA-Token 自动配置类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HelioSaTokenProperties.class})
@AutoConfiguration
public class HelioSaTokenAutoConfiguration {

    @Conditional(value = OnLocalCacheDaoEnabled.class)
    @ConditionalOnMissingBean(value = SaTokenDao.class)
    @Primary
    @Bean
    public SaTokenDao saTokenLocalCacheDao(HelioSaTokenProperties props) {
        var subProp = props.getRedisDaoWithLocalCache();
        return new SaTokenRedisDaoWithLocalCache(subProp.getDuration(), subProp.getCapacity());
    }

    private static class OnLocalCacheDaoEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = Objects.requireNonNull(context.getBeanFactory()).getBean(HelioSaTokenProperties.class);
            var subProp = props.getRedisDaoWithLocalCache();
            return Boolean.TRUE.equals(subProp.getEnabled());
        }
    }

}
