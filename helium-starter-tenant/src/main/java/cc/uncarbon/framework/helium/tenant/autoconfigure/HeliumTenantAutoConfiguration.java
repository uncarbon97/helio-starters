package cc.uncarbon.framework.helium.tenant.autoconfigure;

import cc.uncarbon.framework.helium.tenant.aop.TenantIgnoreAspect;
import cc.uncarbon.framework.helium.tenant.async.TenantContextTaskDecorator;
import cc.uncarbon.framework.helium.tenant.datasource.TenantDataSourceConfiguration;
import cc.uncarbon.framework.helium.tenant.line.TenantLineConfiguration;
import cc.uncarbon.framework.helium.tenant.props.HeliumTenantProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

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

    public static final String TENANT_CONTEXT_TASK_DECORATOR_BEAN_NAME = "tenantContextTaskDecorator";

    /**
     * 租户忽略切面
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantIgnoreAspect tenantIgnoreAspect() {
        return new TenantIgnoreAspect();
    }

    /**
     * 租户上下文任务装饰器
     * 供 @Async / 自建线程池包装子线程，恢复提交时刻的租户上下文快照
     */
    @Bean
    @ConditionalOnMissingBean(name = TENANT_CONTEXT_TASK_DECORATOR_BEAN_NAME)
    public TenantContextTaskDecorator tenantContextTaskDecorator() {
        return new TenantContextTaskDecorator();
    }
}
