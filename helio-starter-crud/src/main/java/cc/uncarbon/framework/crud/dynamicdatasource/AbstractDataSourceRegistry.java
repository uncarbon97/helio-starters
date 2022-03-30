package cc.uncarbon.framework.crud.dynamicdatasource;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

/**
 * 抽象数据源登记处
 *
 * @author chill
 * @author Uncarbon
 */
@RequiredArgsConstructor
public abstract class AbstractDataSourceRegistry {

    protected final DynamicRoutingDataSource dynamicRoutingDataSource;
    protected final HikariDataSourceCreator dataSourceCreator;

    /**
     * 加入数据源缓存
     *
     * @param dataSourceName 数据源名称
     */
    public void registerDataSource(String dataSourceName) {
        /*
        配置不存在则动态添加数据源，以懒加载的模式解决分布式场景的配置同步
        为了保证数据完整性，配置后生成数据源缓存，后台便无法修改更换数据源，若一定要修改请迁移数据后重启服务或自行修改底层逻辑
         */
        if (!dynamicRoutingDataSource.getDataSources().containsKey(dataSourceName)) {
            DataSourceDefinition dataSourceDefinition = this.getDataSourceDefinition(dataSourceName);
            if (dataSourceDefinition != null) {
                // 拷贝数据源配置
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
            }
        }
    }

    /**
     * 根据需要的数据源名称，得到数据源定义
     * @param requiredDataSourceName 需要的数据源名称
     * @return DataSourceDefinition
     */
    public abstract DataSourceDefinition getDataSourceDefinition(String requiredDataSourceName);

}
