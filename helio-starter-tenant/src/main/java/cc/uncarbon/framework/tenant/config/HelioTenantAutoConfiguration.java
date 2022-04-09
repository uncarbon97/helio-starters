package cc.uncarbon.framework.tenant.config;

import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import cc.uncarbon.framework.crud.support.impl.DefaultTenantSupport;
import cc.uncarbon.framework.tenant.support.TenantDataSourceSupport;
import cc.uncarbon.framework.tenant.support.TenantLineSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Helio 多租户自动配置类
 * 当启用多租户功能，且多租户隔离级别配置正确，则向 IoC 容器注入对应处理 Bean
 */
@Configuration
@RequiredArgsConstructor
public class HelioTenantAutoConfiguration {

    private final HelioProperties helioProperties;


    @Bean
    public TenantSupport tenantSupport(GenericApplicationContext applicationContext) {
        if (!Boolean.TRUE.equals(helioProperties.getTenant().getEnabled())) {
            // 引入了 starter，但未启用多租户
            return new DefaultTenantSupport();
        }

        switch (helioProperties.getTenant().getIsolateLevel()) {
            case LINE:
                // 行级
                return new TenantLineSupport();
            case DATASOURCE:
                // 数据源级
                return new TenantDataSourceSupport();
            default:
                throw new IllegalArgumentException("启用多租户功能后，请正确配置对应的多租户隔离级别 (helio.tenant.isolate-level) ");
        }
    }

}
