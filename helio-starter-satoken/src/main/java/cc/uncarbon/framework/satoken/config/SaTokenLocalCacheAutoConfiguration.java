package cc.uncarbon.framework.satoken.config;

import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.satoken.dao.SaTokenLocalCacheDao;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * SA-Token 本地缓存自动配置类
 *
 * @author Uncarbon
 */
@ConditionalOnExpression(value = "${helio.security.saTokenLocalCache.enabled:false} || ${helio.security.sa-token-local-cache.enabled:false}")
@AutoConfiguration
public class SaTokenLocalCacheAutoConfiguration {

    @ConditionalOnMissingBean(value = SaTokenLocalCacheDao.class)
    @Primary
    @Bean
    public SaTokenLocalCacheDao saTokenLocalCacheDao(HelioProperties helioProperties) {
        HelioProperties.Security.SaTokenLocalCache props = helioProperties.getSecurity().getSaTokenLocalCache();
        return new SaTokenLocalCacheDao(props.getDuration(), props.getCapacity());
    }
}
