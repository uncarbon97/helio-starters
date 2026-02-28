package cc.uncarbon.framework.helio.data.config;

import cc.uncarbon.framework.helio.data.dynamicdatasource.DataSourceDefinitionProvider;
import cc.uncarbon.framework.helio.data.dynamicdatasource.HelioDynamicDataSourceRegistry;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * 动态数据源自动配置类
 *
 * @author Uncarbon
 */
@ConditionalOnClass(name = "com.baomidou.dynamic.datasource.DynamicRoutingDataSource")
public class HelioDynamicDataSourceAutoConfiguration {

    @ConditionalOnBean(value = DynamicRoutingDataSource.class)
    @Bean
    public HelioDynamicDataSourceRegistry helioDynamicDataSourceRegistry(
            DynamicRoutingDataSource dynamicRoutingDataSource,
            HikariDataSourceCreator dataSourceCreator,
            ObjectProvider<DataSourceDefinitionProvider> dataSourceDefinitionProviders
    ) {
        return new HelioDynamicDataSourceRegistry(
                dynamicRoutingDataSource, dataSourceCreator, dataSourceDefinitionProviders);
    }
}
