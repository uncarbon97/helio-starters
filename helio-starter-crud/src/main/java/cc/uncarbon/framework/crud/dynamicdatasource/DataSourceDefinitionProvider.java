package cc.uncarbon.framework.crud.dynamicdatasource;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;

/**
 * 动态数据源定义提供者
 */
public interface DataSourceDefinitionProvider {

    /**
     * 根据数据源名称，得到数据源定义
     * @param dataSourceName 需要的数据源名称
     * @return DataSourceDefinition
     */
    DataSourceDefinition getDataSourceDefinition(String dataSourceName);

    /**
     * 在新数据源实际创建之前，可以修改数据源属性
     * 如：补充设置 HikariCP 的 maxPoolSize、connectionTimeout、idleTimeout 等
     * @param dataSourceProperty 数据源属性
     */
    default void beforeCreateDataSource(DataSourceProperty dataSourceProperty) {}

}
