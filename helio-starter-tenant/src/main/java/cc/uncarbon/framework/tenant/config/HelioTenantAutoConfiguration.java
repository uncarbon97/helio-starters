package cc.uncarbon.framework.tenant.config;

import cc.uncarbon.framework.core.context.TenantContextHolder;
import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import cc.uncarbon.framework.crud.support.impl.DefaultTenantSupport;
import cc.uncarbon.framework.tenant.support.DataSourceTenantSupport;
import cc.uncarbon.framework.tenant.support.LineTenantSupport;
import cc.uncarbon.framework.tenant.support.TableTenantSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Helio 多租户自动配置类
 * 当启用多租户功能，且多租户隔离级别配置正确，则向 IoC 容器注入对应处理 Bean
 */
@Configuration
@RequiredArgsConstructor
public class HelioTenantAutoConfiguration {

    private final HelioProperties helioProperties;


    @Bean
    @Primary
    public TenantSupport tenantSupport() {
        if (!Boolean.TRUE.equals(helioProperties.getTenant().getEnabled())) {
            // 引入了 starter，但未启用多租户
            return new DefaultTenantSupport();
        }

        // 除了 HelioProperties 外，在这里也设置多租户启用标识，一定程度上减少对 HelioProperties 的依赖注入
        TenantContextHolder.setTenantEnabled(true);

        switch (helioProperties.getTenant().getIsolateLevel()) {
            case LINE:
                // 多租户支持-行级
                return new LineTenantSupport();
            case TABLE:
                // 多租户支持-表级
                return new TableTenantSupport();
            case DATASOURCE:
                // 多租户支持-数据源级
                return new DataSourceTenantSupport();
            default:
                throw new IllegalArgumentException("启用多租户功能后，请正确配置对应的多租户隔离级别 (helio.tenant.isolate-level) ");
        }
    }

}
