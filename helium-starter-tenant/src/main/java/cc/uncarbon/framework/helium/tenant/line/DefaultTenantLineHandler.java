package cc.uncarbon.framework.helium.tenant.line;

import cc.uncarbon.framework.helium.db.constant.EntityField;
import cc.uncarbon.framework.helium.db.entity.TenantEntity;
import cc.uncarbon.framework.helium.tenant.annotation.TenantIgnore;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class DefaultTenantLineHandler implements TenantLineHandler {

    /**
     * 显式配置忽略租户隔离的表
     */
    private final Collection<String> ignoredTables;

    /**
     * 表名是否忽略租户的计算结果缓存（表名统一小写作为键）
     */
    private final Map<String, Boolean> ignoredTableCache = new ConcurrentHashMap<>();


    @Override
    public Expression getTenantId() {
        Long currentTenantId = TenantContextHolder.getTenantId();
        if (currentTenantId == null) {
            return new NullValue();
        }
        return new LongValue(currentTenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return EntityField.TENANT_ID_COLUMN;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 全局忽略租户
        if (TenantContextHolder.isIgnored()) {
            return true;
        }
        // 归一化表名：去除包裹符号，统一小写
        String normalizedTableName = normalizedTableName(tableName);
        Boolean ignore = ignoredTableCache.get(normalizedTableName);
        if (ignore == null) {
            ignore = computeIgnoreTable(normalizedTableName);
            ignoredTableCache.put(normalizedTableName, ignore);
        }
        return ignore;
    }

    private boolean computeIgnoreTable(String normalizedTableName) {
        // 显式配置忽略的表
        if (isConfiguredIgnored(normalizedTableName)) {
            return true;
        }

        TableInfo tableInfo = TableInfoHelper.getTableInfo(normalizedTableName);
        if (tableInfo == null) {
            // 找不到对应实体（如纯 XML Mapper 编写的 SQL），保持参与租户隔离，避免遗漏
            return false;
        }
        // 实体类标注忽略注解
        Class<?> entityType = tableInfo.getEntityType();
        if (entityType.isAnnotationPresent(TenantIgnore.class)) {
            return true;
        }
        return !TenantEntity.class.isAssignableFrom(entityType);
    }

    private boolean isConfiguredIgnored(String normalizedTableName) {
        if (ignoredTables == null) {
            return false;
        }
        for (String configuredTableName : ignoredTables) {
            if (normalizedTableName.equalsIgnoreCase(configuredTableName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 去除表名的包裹符号，如 `user`、"user"、[user] -> user
     */
    private static String normalizedTableName(String tableName) {
        return tableName.replaceAll("`|\"|\\[|\\]", "").toLowerCase();
    }

}
