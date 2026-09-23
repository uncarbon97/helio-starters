package cc.uncarbon.framework.helium.tenant.line;

import cc.uncarbon.framework.helium.db.constant.EntityField;
import cc.uncarbon.framework.helium.tenant.annotation.TenantIgnore;
import cc.uncarbon.framework.helium.tenant.props.HeliumTenantProperties;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * 行级多租户启动期强对账（实体口径）
 *
 * <p>口径翻转（{@link HeliumTenantProperties#getParticipateByDefault()}=true）后，将「运行时首条 SQL 报 Unknown column」提前到部署时：
 * 经 JDBC {@link DatabaseMetaData} 逐表比对「参与隔离的实体表必有 {@link EntityField#TENANT_ID_COLUMN} 列」，缺列则启动失败。</p>
 *
 * <p>对账范围：{@link TableInfoHelper} 能扫描到的实体表；无实体类的纯 XML Mapper 表不在对账范围内
 * （运行时仍参与隔离，需自行保证含列）。列存在性经 JDBC 元数据判定，支持 MySQL + PGSQL </p>
 *
 * <p>忽略口径与 {@link DefaultTenantLineHandler} 一致：{@link HeliumTenantProperties#getIgnoredTables()} 配置、实体 {@link TenantIgnore}。</p>
 *
 * @author Uncarbon
 */
@Slf4j
@RequiredArgsConstructor
public class TenantLineFieldReconciler implements ApplicationRunner {

    private static final String LOG_PREFIX = TenantLineConfiguration.LOG_PREFIX;

    private final DataSource dataSource;
    private final HeliumTenantProperties props;

    private static String normalizedTableName(String tableName) {
        return DefaultTenantLineHandler.normalizeTableName(tableName);
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        Set<String> ignoredTables = resolveIgnoredTables();
        Set<String> missing = new HashSet<>();

        try (Connection connection = dataSource.getConnection()) {
            for (TableInfo tableInfo : TableInfoHelper.getTableInfos()) {
                String tableName = tableInfo.getTableName();
                String normalizedTableName = normalizedTableName(tableName);
                if (ignoredTables.contains(normalizedTableName)
                        || tableInfo.getEntityType().isAnnotationPresent(TenantIgnore.class)) {
                    continue;
                }
                if (!hasTenantIdColumn(connection, normalizedTableName)) {
                    missing.add(tableName);
                }
            }
        }

        if (missing.isEmpty()) {
            log.info(LOG_PREFIX + " 启动期对账通过 >> 参与隔离的实体表均含 {} 列；忽略表: {}", EntityField.TENANT_ID_COLUMN, ignoredTables);
            return;
        }

        throw new IllegalStateException(LOG_PREFIX + " 启动期对账失败 >> 以下实体表未显式忽略、但缺少 tenant_id 列（或表尚未创建），无法参与租户隔离: "
                + missing
                + "；请先执行 DDL 升级脚本补充列，或将其加入忽略表 / 实体标注 @TenantIgnore");
    }

    /**
     * 经 JDBC 元数据判断表是否含 tenant_id 列，支持 MySQL + PGSQL
     */
    private boolean hasTenantIdColumn(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String product = metaData.getDatabaseProductName();
        String catalog;
        String schemaPattern;
        if (product != null && product.toLowerCase().contains("postgres")) {
            catalog = null;
            schemaPattern = connection.getSchema();
        } else {
            catalog = connection.getCatalog();
            schemaPattern = null;
        }
        try (ResultSet rs = metaData.getColumns(catalog, schemaPattern, tableName, EntityField.TENANT_ID_COLUMN)) {
            return rs.next();
        }
    }

    /**
     * 汇总配置口径忽略的表名（统一小写）
     */
    private Set<String> resolveIgnoredTables() {
        Set<String> ret = new HashSet<>();
        if (props.getIgnoredTables() != null) {
            props.getIgnoredTables().forEach(table -> ret.add(normalizedTableName(table)));
        }
        return ret;
    }
}
