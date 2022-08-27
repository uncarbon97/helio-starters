package cc.uncarbon.framework.crud.dynamicdatasource;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Helio 动态多数据源登记处
 *
 * @author chill
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public class HelioDynamicDataSourceRegistry {

    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final HikariDataSourceCreator dataSourceCreator;
    private final ObjectProvider<DataSourceDefinitionProvider> dataSourceDefinitionProviders;


    /**
     * 查询是否已存在数据源
     *
     * @param dataSourceName 数据源名称
     * @return boolean
     */
    public boolean containsDataSource(String dataSourceName) {
        return this.containsDataSource(dataSourceName, false);
    }

    /**
     * 查询是否已存在数据源
     *
     * @param dataSourceName 数据源名称
     * @param obtainIfAbsent 缺失时尝试从 IoC 容器中获取，并加入数据源缓存
     * @return boolean
     */
    public boolean containsDataSource(String dataSourceName, boolean obtainIfAbsent) {
        /*
        配置不存在则动态添加数据源，以懒加载的模式解决分布式场景的配置同步
        为了保证数据完整性，配置后生成数据源缓存，后台便无法修改更换数据源，若一定要修改请迁移数据后重启服务或自行修改底层逻辑
         */
        if (dynamicRoutingDataSource.getDataSources().containsKey(dataSourceName)) {
            return true;
        }

        if (!obtainIfAbsent) {
            return false;
        }

        boolean successFlag = false;
        for (DataSourceDefinitionProvider provider : dataSourceDefinitionProviders) {
            DataSourceDefinition dataSourceDefinition = provider.getDataSourceDefinition(dataSourceName);

            if (dataSourceDefinition != null) {
                // 存在非空结果，则拷贝数据源配置
                DataSourceProperty dataSourceProperty = new DataSourceProperty();
                BeanUtil.copyProperties(dataSourceDefinition, dataSourceProperty);

                // 补充字段
                dataSourceProperty
                        .setPoolName(dataSourceDefinition.getName())
                        .setType(HikariDataSource.class)
                ;

                // 创建动态数据源
                DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);

                // 添加新数据源
                dynamicRoutingDataSource.addDataSource(dataSourceName, dataSource);
                log.info("已动态添加新数据源 {}", dataSourceName);

                successFlag = true;
                break;
            }
        }

        if (!successFlag) {
            log.error("动态添加新数据源失败，原因：没有 DataSourceDefinitionProvider 提供 {} 的 DataSourceDefinition", dataSourceName);
        }

        return successFlag;
    }

}
