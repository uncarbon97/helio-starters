package cc.uncarbon.framework.helium.db.dynamicdatasource.helper;

import cc.uncarbon.framework.helium.db.model.DataSourceSetting;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;

import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * 租户数据源助手类
 *
 * @author Charles7c@continew
 * @author Uncarbon
 */
public interface DynamicDataSourceHelper {

    /**
     * 指定数据源是否已注册
     *
     * @param alias 数据源别名
     * @return 是否已注册
     */
    boolean hasDataSource(String alias);

    /**
     * 切换至数据源
     * 如果数据源未注册，则会切换失败
     *
     * @param alias 数据源别名
     * @return 是否成功切换
     */
    boolean switchToDataSource(String alias);

    /**
     * 切换至数据源
     *
     * @param alias 数据源别名
     * @param settingSupplier 如果数据源未注册，会尝试从 settingSupplier 获取属性配置并注册后，再尝试切换
     * @return 是否成功切换
     */
    boolean switchToDataSource(String alias, Supplier<DataSourceSetting> settingSupplier);

    /**
     * 注册数据源
     *
     * @param setting 数据源属性配置
     * @return 数据源
     */
    DataSource registerDataSource(DataSourceSetting setting);

    /**
     * 注册数据源
     *
     * @param alias 数据源别名，会强制覆盖掉 dataSourceProperty.poolName 字段
     * @param dataSourceProperty 数据源配置
     * @return 数据源
     */
    DataSource registerDataSource(String alias, DataSourceProperty dataSourceProperty);

    /**
     * 取消注册数据源
     *
     * @param alias 数据源别名
     */
    void unregisterDataSource(String alias);

    /**
     * 清空当前线程数据源
     */
    default void poll() {
        DynamicDataSourceContextHolder.poll();
    }

}
