package cc.uncarbon.framework.helium.tenant.line;

import cc.uncarbon.framework.helium.db.constant.EntityField;
import cc.uncarbon.framework.helium.tenant.annotation.TenantIgnore;
import cc.uncarbon.framework.helium.tenant.props.HeliumTenantProperties;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
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

    protected static final String LOG_PREFIX = TenantLineConfiguration.LOG_PREFIX;

    protected final DataSource dataSource;
    protected final HeliumTenantProperties props;

    protected static String normalizedTableName(String tableName) {
        return DefaultTenantLineHandler.normalizeTableName(tableName);
    }

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        Set<String> ignoredTables = resolveIgnoredTables();
        Set<String> missing = new HashSet<>();
        // 含 tenantId 字段但未配 fill 的参与实体：自动填充不会覆盖请求体携带的租户值，存在跨租户写入风险
        Set<String> tenantFieldWithoutFill = new HashSet<>();

        try (Connection connection = dataSource.getConnection()) {
            for (TableInfo tableInfo : TableInfoHelper.getTableInfos()) {
                String tableName = tableInfo.getTableName();
                String normalizedTableName = normalizedTableName(tableName);
                if (ignoredTables.contains(normalizedTableName)
                        || tableInfo.getEntityType().isAnnotationPresent(TenantIgnore.class)) {
                    continue;
                }
                if (!hasTenantIdColumn(connection, tableName, normalizedTableName)) {
                    missing.add(tableName);
                }
                if (tenantFieldMissingFill(tableInfo)) {
                    tenantFieldWithoutFill.add(tableInfo.getEntityType().getName());
                }
            }
        }

        if (!tenantFieldWithoutFill.isEmpty()) {
            log.warn(LOG_PREFIX + " 以下参与隔离的实体含 " + EntityField.TENANT_ID_FIELD + " 字段但未配置 " +
                    "@TableField(fill = INSERT) 自动填充，存在跨租户写入风险 >> {}", tenantFieldWithoutFill);
        }

        if (missing.isEmpty()) {
            log.info(LOG_PREFIX + " 启动期对账通过 >> 参与隔离的实体表均含 {} 列；忽略表: {}", EntityField.TENANT_ID_COLUMN, ignoredTables);
            return;
        }

        if (dataSource instanceof DynamicRoutingDataSource) {
            /*
            动态数据源场景仅对账 primary 库，其余库的表分布未知：缺列不一定真是缺陷（表可能在租户库），
            降级为 error 提示不阻塞启动，由部署方确认
             */
            log.warn(LOG_PREFIX + " 启动期对账发现疑似缺列实体表（当前为动态数据源，仅对账 primary 库）: {}" +
                    "  请确认这些表所在库均含 " + EntityField.TENANT_ID_COLUMN + "列，或将其加入忽略表 / 实体标注 @TenantIgnore",
                    missing);
            return;
        }

        throw new IllegalStateException(LOG_PREFIX + " 启动期对账失败 >> 以下实体表未显式忽略、但缺少 "
                + EntityField.TENANT_ID_COLUMN + "列（或表尚未创建），无法参与租户隔离: " + missing
                + "；请先执行 DDL 升级脚本补充列，或将其加入忽略表 / 实体标注 @TenantIgnore");
    }

    /**
     * 实体含 tenantId 字段但未配置 INSERT/INSERT_UPDATE 填充：
     * insertFill 的强制覆盖不生效（MP 仅对带 fill 注解的实体触发填充），请求体携带的租户值会被原样写入
     */
    protected boolean tenantFieldMissingFill(TableInfo tableInfo) {
        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (EntityField.TENANT_ID_FIELD.equals(fieldInfo.getProperty())
                    && fieldInfo.getFieldFill() != FieldFill.INSERT
                    && fieldInfo.getFieldFill() != FieldFill.INSERT_UPDATE) {
                return true;
            }
        }
        return false;
    }

    /**
     * 经 JDBC 元数据判断表是否含 tenant_id 列，支持 MySQL + PGSQL
     */
    protected boolean hasTenantIdColumn(Connection connection, String originalTableName, String normalizedTableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String product = metaData.getDatabaseProductName();
        String catalog;
        String schemaPattern;
        if (product != null && product.toLowerCase().contains("postgres")) {
            catalog = null;
            schemaPattern = connection.getSchema();
            if (schemaPattern == null) {
                /*
                部分驱动 getSchema() 返回 null；此时 getColumns 会跨全部 schema 匹配，
                同名表在其他 schema 含 tenant_id 会导致漏报（对账假通过），限定为 current_schema()
                 */
                schemaPattern = queryCurrentSchema(connection);
            }
        } else {
            catalog = connection.getCatalog();
            schemaPattern = null;
        }
        /*
        先按原始表名匹配（保留大小写，适配 lower_case_table_names=0 的 MySQL 混合大小写表），
        未命中再按归一化小写表名匹配（适配 PG 等全小写存储）；两路任一命中即视为含列
         */
        if (existsColumn(metaData, catalog, schemaPattern, originalTableName)) {
            return true;
        }
        return existsColumn(metaData, catalog, schemaPattern, normalizedTableName);
    }

    protected boolean existsColumn(DatabaseMetaData metaData, String catalog, String schemaPattern, String tableName) throws SQLException {
        /*
        JDBC 元数据匹配中表名是模式（`_` 为单字符通配符），可能匹配到同名变体表导致对账假通过；
        结果集内再用实际表名精确比对（忽略大小写）一次
         */
        try (ResultSet rs = metaData.getColumns(catalog, schemaPattern, tableName, EntityField.TENANT_ID_COLUMN)) {
            while (rs.next()) {
                String matchedTableName = rs.getString("TABLE_NAME");
                if (tableName.equalsIgnoreCase(matchedTableName)) {
                    return true;
                }
            }
            return false;
        }
    }

    protected String queryCurrentSchema(Connection connection) {
        try (var statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT current_schema()")) {
            return rs.next() ? rs.getString(1) : null;
        } catch (SQLException ignore) {
            return null;
        }
    }

    /**
     * 汇总配置口径忽略的表名（统一小写）
     */
    protected Set<String> resolveIgnoredTables() {
        Set<String> ret = new HashSet<>();
        if (props.getIgnoredTables() != null) {
            props.getIgnoredTables().forEach(table -> ret.add(normalizedTableName(table)));
        }
        return ret;
    }
}
