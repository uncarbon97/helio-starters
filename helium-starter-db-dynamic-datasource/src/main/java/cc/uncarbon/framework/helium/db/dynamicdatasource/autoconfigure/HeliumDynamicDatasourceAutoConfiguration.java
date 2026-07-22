package cc.uncarbon.framework.helium.db.dynamicdatasource.autoconfigure;

import cc.uncarbon.framework.helium.db.dynamicdatasource.helper.DynamicDataSourceHelper;
import cc.uncarbon.framework.helium.db.dynamicdatasource.helper.DynamicDataSourceHelperImpl;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Helium 集成 dynamic-datasource 自动装配类
 *
 * @author Uncarbon
 */
@AutoConfigureAfter(value = {DynamicDataSourceAutoConfiguration.class})
@AutoConfiguration
public class HeliumDynamicDatasourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceHelper dynamicDataSourceHelper(DynamicRoutingDataSource dynamicRoutingDataSource,
                                                           HikariDataSourceCreator dataSourceCreator) {
        return new DynamicDataSourceHelperImpl(dynamicRoutingDataSource, dataSourceCreator);
    }
}
