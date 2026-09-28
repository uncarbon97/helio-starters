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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 租户数据源助手类
 *
 * @author Charles7c@continew
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public class DynamicDataSourceHelperImpl implements DynamicDataSourceHelper {

    private static final String LOG_PREFIX = "[Framework][动态数据源]";

    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final HikariDataSourceCreator dataSourceCreator;

    /**
     * 按 alias 粒度的注册锁，防止并发首次注册重复建池（check-then-act 竞态）
     */
    private final ConcurrentHashMap<String, Object> aliasLocks = new ConcurrentHashMap<>();


    @Override
    public boolean hasDataSource(String alias) {
        return CharSequenceUtil.isNotEmpty(alias) && dynamicRoutingDataSource.getDataSources()
                .containsKey(alias);
    }

    @Override
    public boolean switchToDataSource(String alias) {
        if (hasDataSource(alias)) {
            DynamicDataSourceContextHolder.push(alias);
            log.info(LOG_PREFIX + " 切换至数据源: {}", alias);
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
        Object lock = aliasLocks.computeIfAbsent(alias, k -> new Object());
        synchronized (lock) {
            dataSourceProperty.setPoolName(alias);
            DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);
            if (dataSource != null) {
                try {
                    // 必须加入路由表，否则 switchToDataSource 永远找不到该数据源
                    dynamicRoutingDataSource.addDataSource(alias, dataSource);
                    log.info(LOG_PREFIX + " 注册了数据源: {}", alias);
                } catch (Exception e) {
                    // 注册失败，关闭连接池避免孤儿池泄漏
                    if (dataSource instanceof AutoCloseable closeable) {
                        try {
                            closeable.close();
                        } catch (Exception ignore) {
                            // 关闭失败仅忽略，保留原异常
                        }
                    }
                    throw (RuntimeException) e;
                }
            }
            return dataSource;
        }
    }

    @Override
    public void unregisterDataSource(String alias) {
        if (CharSequenceUtil.isBlank(alias)) {
            throw new IllegalArgumentException("数据源别名不能为空");
        }
        try {
            dynamicRoutingDataSource.removeDataSource(alias);
        } catch (RuntimeException e) {
            // 库内部异常（如删除 primary 数据源）补充上下文后抛出，给出可读信息
            throw new IllegalArgumentException("注销数据源失败: " + alias + "，原因: " + e.getMessage(), e);
        }
    }
}
