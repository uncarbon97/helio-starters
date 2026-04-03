package cc.uncarbon.framework.helio.satoken.autoconfigure;

import cc.uncarbon.framework.helio.base.autoconfigure.HelioProperties;
import cc.uncarbon.framework.helio.satoken.dao.SaTokenLocalCacheDao;
import cn.dev33.satoken.dao.SaTokenDao;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

/**
 * Helio 关于 SA-Token 自动配置类
 *
 * @author Uncarbon
 */
@AutoConfiguration
public class HelioSaTokenAutoConfiguration {

    @Conditional(value = OnLocalCacheDaoEnabled.class)
    @ConditionalOnMissingBean(value = SaTokenDao.class)
    @Primary
    @Bean
    public SaTokenDao saTokenLocalCacheDao(HelioProperties helioProperties) {
        var subProp = helioProperties.getSatoken().getLocalCacheDao();
        return new SaTokenLocalCacheDao(subProp.getDuration(), subProp.getCapacity());
    }

    private static class OnLocalCacheDaoEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            HelioProperties helioProperties = Objects.requireNonNull(context.getBeanFactory()).getBean(HelioProperties.class);
            return Boolean.TRUE.equals(helioProperties.getSatoken().getLocalCacheDao().getEnabled());
        }
    }

}
