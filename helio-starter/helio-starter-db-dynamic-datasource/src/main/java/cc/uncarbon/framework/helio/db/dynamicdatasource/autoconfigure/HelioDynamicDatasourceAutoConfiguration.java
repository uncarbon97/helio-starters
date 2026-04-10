package cc.uncarbon.framework.helio.db.dynamicdatasource.autoconfigure;

import cc.uncarbon.framework.helio.db.dynamicdatasource.dynamicdatasource.DataSourceDefinitionProvider;
import cc.uncarbon.framework.helio.db.dynamicdatasource.dynamicdatasource.HelioDynamicDataSourceRegistry;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

/**
 * Helio 集成 dynamic-datasource 自动配置类
 *
 * @author Uncarbon
 */
public class HelioDynamicDatasourceAutoConfiguration {

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
