package cc.uncarbon.framework.helium.db.dynamicdatasource.helper;

import cc.uncarbon.framework.helium.db.model.DataSourceSetting;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * 租户数据源助手类
 *
 * @author Charles7c
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public class DynamicDataSourceHelperImpl implements DynamicDataSourceHelper {

    private static final String LOG_PREFIX = "[Framework][动态数据源]";

    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final HikariDataSourceCreator dataSourceCreator;


    @Override
    public boolean hasDataSource(String alias) {
        return CharSequenceUtil.isNotEmpty(alias) && dynamicRoutingDataSource.getDataSources()
                .containsKey(alias);
    }

    @Override
    public boolean switchToDataSource(String alias) {
        if (hasDataSource(alias)) {
            DynamicDataSourceContextHolder.push(alias);
            log.info(LOG_PREFIX + " 切换至数据源【{}】", alias);
            return true;
        }
        return false;
    }

    @Override
    public boolean switchToDataSource(String alias, Supplier<DataSourceSetting> settingSupplier) {
        if (!hasDataSource(alias) && settingSupplier != null) {
            DataSourceSetting dataSourceSetting = settingSupplier.get();
            if (dataSourceSetting != null) {
                dataSourceSetting.setAlias(alias);
                registerDataSource(dataSourceSetting);
            }
        }
        return switchToDataSource(alias);
    }

    @Override
    public DataSource registerDataSource(DataSourceSetting setting) {
        DataSourceProperty property = new DataSourceProperty();
        BeanUtil.copyProperties(setting, property);
        return registerDataSource(setting.getAlias(), property);
    }

    @Override
    public DataSource registerDataSource(String alias, DataSourceProperty dataSourceProperty) {
        dataSourceProperty.setPoolName(alias);
        DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);
        if (dataSource != null) {
            log.info(LOG_PREFIX + " 注册了数据源【{}】", alias);
        }
        return dataSource;
    }

    @Override
    public void unregisterDataSource(String alias) {
        dynamicRoutingDataSource.removeDataSource(alias);
    }
}
