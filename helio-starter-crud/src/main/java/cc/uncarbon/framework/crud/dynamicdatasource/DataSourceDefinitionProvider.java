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

    /**
     * 在通过 baomidou/dynamic-datasource 创建数据源之前，修改配置
     * 如：补充设置 HikariCP 的 maxPoolSize、connectionTimeout、idleTimeout 等
     * @param dataSourceProperty 数据源配置；为了减少不必要的外部依赖，需要强行转换为 com.baomidou.dynamic.datasource.creator.DataSourceProperty 后，再赋值对象字段
     */
    default void beforeCreateDataSourceThroughBaomidou(Object dataSourceProperty) {}

}
