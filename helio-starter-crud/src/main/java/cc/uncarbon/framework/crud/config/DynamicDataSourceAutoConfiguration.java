package cc.uncarbon.framework.crud.config;

import cc.uncarbon.framework.crud.dynamicdatasource.DataSourceDefinitionProvider;
import cc.uncarbon.framework.crud.dynamicdatasource.HelioDynamicDataSourceRegistry;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.HikariDataSourceCreator;
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
public class DynamicDataSourceAutoConfiguration {

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
