package cc.uncarbon.framework.helio.db.dynamicdatasource.autoconfigure;

import cc.uncarbon.framework.helio.db.dynamicdatasource.helper.DynamicDataSourceHelper;
import cc.uncarbon.framework.helio.db.dynamicdatasource.helper.DynamicDataSourceHelperImpl;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Helio 集成 dynamic-datasource 自动配置类
 *
 * @author Uncarbon
 */
@AutoConfiguration
public class HelioDynamicDatasourceAutoConfiguration {


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
