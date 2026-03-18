package cc.uncarbon.framework.helio.satoken.autoconfigure;

import cc.uncarbon.framework.helio.base.props.HelioProperties;
import cc.uncarbon.framework.helio.satoken.dao.SaTokenLocalCacheDao;
import cn.dev33.satoken.dao.SaTokenDao;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

/**
 * SA-Token 本地缓存自动配置类
 *
 * @author Uncarbon
 */
@AutoConfiguration
public class SaTokenLocalCacheDaoAutoConfiguration {

    @Conditional(value = ConditionalOnLocalCacheDaoEnabled.class)
    @ConditionalOnMissingBean(value = SaTokenDao.class)
    @Primary
    @Bean
    public SaTokenDao saTokenLocalCacheDao(HelioProperties helioProperties) {
        var subProp = helioProperties.getSatoken().getLocalCacheDao();
        return new SaTokenLocalCacheDao(subProp.getDuration(), subProp.getCapacity());
    }

    private static class ConditionalOnLocalCacheDaoEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            HelioProperties helioProperties = Objects.requireNonNull(context.getBeanFactory()).getBean(HelioProperties.class);
            return helioProperties.getSatoken().getLocalCacheDao().isEnabled();
        }
    }

}
