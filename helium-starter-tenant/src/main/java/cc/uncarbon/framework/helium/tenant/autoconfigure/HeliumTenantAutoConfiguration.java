package cc.uncarbon.framework.helium.tenant.autoconfigure;

import cc.uncarbon.framework.helium.tenant.aop.TenantIgnoreAspect;
import cc.uncarbon.framework.helium.tenant.datasource.TenantDataSourceConfiguration;
import cc.uncarbon.framework.helium.tenant.line.TenantLineConfiguration;
import cc.uncarbon.framework.helium.tenant.props.HeliumTenantProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

/**
 * Helium 多租户自动装配类
 *
 * @author Uncarbon
 */
@Import(value = {TenantLineConfiguration.class, TenantDataSourceConfiguration.class})
@EnableConfigurationProperties(value = {HeliumTenantProperties.class})
@AutoConfiguration
@Slf4j
public class HeliumTenantAutoConfiguration {

    /**
     * 租户忽略切面
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantIgnoreAspect tenantIgnoreAspect() {
        return new TenantIgnoreAspect();
    }
}
