package cc.uncarbon.framework.helio.tenant.autoconfigure;

import cc.uncarbon.framework.helio.tenant.aop.TenantIgnoreAspect;
import cc.uncarbon.framework.helio.tenant.datasource.TenantDataSourceConfiguration;
import cc.uncarbon.framework.helio.tenant.line.TenantLineConfiguration;
import cc.uncarbon.framework.helio.tenant.props.HelioTenantProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

/**
 * Helio 多租户自动配置类
 *
 * @author Uncarbon
 */
@Import(value = {TenantLineConfiguration.class, TenantDataSourceConfiguration.class})
@EnableConfigurationProperties(value = {HelioTenantProperties.class})
@AutoConfiguration
@Slf4j
public class HelioTenantAutoConfiguration {

    /**
     * 租户忽略切面
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantIgnoreAspect tenantIgnoreAspect() {
        return new TenantIgnoreAspect();
    }
}
