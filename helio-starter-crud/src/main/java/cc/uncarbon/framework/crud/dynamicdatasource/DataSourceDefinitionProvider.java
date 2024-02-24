package cc.uncarbon.framework.crud.dynamicdatasource;

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

}
