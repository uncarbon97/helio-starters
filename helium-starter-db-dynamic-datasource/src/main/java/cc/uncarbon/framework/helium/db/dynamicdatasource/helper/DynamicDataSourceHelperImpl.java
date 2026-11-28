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
@Slf4j
@RequiredArgsConstructor
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
        if (CharSequenceUtil.isBlank(alias)) {
            throw new IllegalArgumentException("数据源别名不能为空");
        }
        Object lock = aliasLocks.computeIfAbsent(alias, k -> new Object());
        boolean registered = false;
        try {
            synchronized (lock) {
                // double-check：等锁期间可能已被其他线程注册，避免重复建池
                DataSource existing = dynamicRoutingDataSource.getDataSources().get(alias);
                if (existing != null) {
                    registered = true;
                    return existing;
                }
                dataSourceProperty.setPoolName(alias);
                DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);
                if (dataSource != null) {
                    try {
                        // 必须加入路由表，否则 switchToDataSource 永远找不到该数据源
                        dynamicRoutingDataSource.addDataSource(alias, dataSource);
                        registered = true;
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
                        // checked 异常不能强转 RuntimeException（会 CCE 丢失根因），统一包装
                        throw e instanceof RuntimeException re
                                ? re
                                : new IllegalStateException("注册数据源失败: " + alias, e);
                    }
                }
                return dataSource;
            }
        } finally {
            if (!registered) {
                // 注册未成功（建池失败 / 返回 null / 加入路由表失败）时清理锁，避免 alias 由外部输入时无界增长
                aliasLocks.remove(alias, lock);
            }
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
        } finally {
            // 注销后清理注册锁，避免频繁注册/注销场景下 aliasLocks 无界增长
            aliasLocks.remove(alias);
        }
    }
}
