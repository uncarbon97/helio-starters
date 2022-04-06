package cc.uncarbon.framework.crud.config;

import cc.uncarbon.framework.crud.dynamicdatasource.DataSourceDefinitionProvider;
import cc.uncarbon.framework.crud.dynamicdatasource.HelioDynamicDataSourceRegistry;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.HikariDataSourceCreator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

public class DynamicDataSourceAutoConfiguration {


    @Bean
    public HelioDynamicDataSourceRegistry helioDynamicDataSourceRegistry(
            DynamicRoutingDataSource dynamicRoutingDataSource,
            HikariDataSourceCreator dataSourceCreator,
            ObjectProvider<DataSourceDefinitionProvider> dataSourceDefinitionProviders
    ) {
        return new HelioDynamicDataSourceRegistry(dynamicRoutingDataSource, dataSourceCreator,dataSourceDefinitionProviders);
    }
}
