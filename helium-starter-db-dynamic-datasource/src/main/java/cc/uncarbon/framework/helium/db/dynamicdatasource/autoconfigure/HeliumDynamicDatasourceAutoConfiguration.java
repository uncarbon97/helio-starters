package cc.uncarbon.framework.helium.db.dynamicdatasource.autoconfigure;

import cc.uncarbon.framework.helium.db.dynamicdatasource.helper.DynamicDataSourceHelper;
import cc.uncarbon.framework.helium.db.dynamicdatasource.helper.DynamicDataSourceHelperImpl;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Helium 集成 dynamic-datasource 自动装配类
 *
 * @author Uncarbon
 */
@AutoConfiguration
public class HeliumDynamicDatasourceAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public DynamicRoutingDataSource dynamicRoutingDataSource(DynamicDataSourceProvider ymlDynamicDataSourceProvider) {
        return new DynamicRoutingDataSource(List.of(ymlDynamicDataSourceProvider));
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceHelper dynamicDataSourceHelper(DynamicRoutingDataSource dynamicRoutingDataSource,
                                                           HikariDataSourceCreator dataSourceCreator) {
        return new DynamicDataSourceHelperImpl(dynamicRoutingDataSource, dataSourceCreator);
    }
}
