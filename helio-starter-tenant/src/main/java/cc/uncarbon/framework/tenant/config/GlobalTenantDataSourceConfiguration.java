package cc.uncarbon.framework.tenant.config;

import cc.uncarbon.framework.crud.dynamicdatasource.HelioDynamicDataSourceRegistry;
import cc.uncarbon.framework.tenant.tenantdatasource.GlobalTenantDataSourceAdvisor;
import cc.uncarbon.framework.tenant.tenantdatasource.GlobalTenantDataSourceInterceptor;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

/**
 * 基于全局 AOP 的数据源级多租户配置类
 *
 * @author Uncarbon
 */
public class GlobalTenantDataSourceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DynamicRoutingDataSource dynamicRoutingDataSource() {
        return new DynamicRoutingDataSource();
    }

    @Bean
    @ConditionalOnMissingBean
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    public GlobalTenantDataSourceInterceptor interceptor(
            HelioDynamicDataSourceRegistry dataSourceRegistry
    ) {
        return new GlobalTenantDataSourceInterceptor(dataSourceRegistry);
    }


    @Bean
    @ConditionalOnMissingBean
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    public GlobalTenantDataSourceAdvisor tenantDataSourceGlobalAdvisor(
            GlobalTenantDataSourceInterceptor interceptor,
            DynamicDataSourceProperties properties
    ) {
        GlobalTenantDataSourceAdvisor advisor = new GlobalTenantDataSourceAdvisor(interceptor);
        // 数值越高，优先度越低
        advisor.setOrder(properties.getAop().getOrder() + 1);
        return advisor;
    }

}
