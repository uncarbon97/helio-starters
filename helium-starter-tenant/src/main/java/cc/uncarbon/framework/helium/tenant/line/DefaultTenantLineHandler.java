package cc.uncarbon.framework.helium.tenant.line;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.db.constant.EntityField;
import cc.uncarbon.framework.helium.db.entity.TenantEntity;
import cc.uncarbon.framework.helium.tenant.annotation.TenantIgnore;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
import cc.uncarbon.framework.helium.tenant.props.HeliumTenantProperties;
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

/**
 * 默认行级多租户处理器
 *
 * <p>表忽略口径（按优先级）：
 * <ol>
 *      <li>作用域内忽略（{@code TenantContextHolder#isIgnored()} / {@code @TenantIgnore} 注解切面）</li>
 *      <li>配置 ignored-tables 显式忽略</li>
 *      <li>实体标注 {@link TenantIgnore} 忽略</li>
 *      <li>{@code participateByDefault=true}：其余表一律参与隔离（口径翻转，缺列会启动期强对账拦截）</li>
 *      <li>{@code participateByDefault=false}（兼容默认）：实体实现 {@link TenantEntity} 才参与；纯 XML Mapper 表参与（防漏）</li>
 * </ol>
 */
@RequiredArgsConstructor
public class DefaultTenantLineHandler implements TenantLineHandler {

    /**
     * 显式配置忽略租户隔离的表
     */
    private final Collection<String> ignoredTables;

    /**
     * 默认参与隔离（口径翻转开关）
     */
    private final boolean participateByDefault;

    /**
     * 严格模式：上下文缺租户且 SQL 需要拼租户时抛出业务异常
     */
    private final boolean strict;

    /**
     * 无租户上下文时的兜底写值（非严格模式下生效）
     */
    private final Long defaultTenantId;

    /**
     * 表名是否忽略租户的计算结果缓存（表名统一小写作为键）
     */
    private final Map<String, Boolean> ignoredTableCache = new ConcurrentHashMap<>();


    public DefaultTenantLineHandler(HeliumTenantProperties props) {
        this(props.getIgnoredTables(), props.isParticipateByDefault(),
                props.getStrict(), props.getDefaultTenantId());
    }

    /**
     * 去除表名的包裹符号，如 `user`、"user"、[user] -> user
     */
    public static String normalizeTableName(String tableName) {
        return tableName.replaceAll("`|\"|\\[|\\]", "").toLowerCase();
    }

    @Override
    public String getTenantIdColumn() {
        return EntityField.TENANT_ID_COLUMN;
    }

    @Override
    public Expression getTenantId() {
        Long currentTenantId = TenantContextHolder.getTenantId();
        if (currentTenantId == null) {
            if (strict) {
                // 子线程漏传上下文等场景，响亮失败优于静默空结果
                throw new IllegalStateException("当前线程缺少租户上下文，且已开启严格模式("
                        + ConfigurationPropertiesPrefix.TENANT + ".strict)");
            }
            if (defaultTenantId != null) {
                // 兜底写值，如平台自营域
                return new LongValue(defaultTenantId);
            }
            return new NullValue();
        }
        return new LongValue(currentTenantId);
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 作用域内忽略租户
        if (TenantContextHolder.isIgnored()) {
            return true;
        }
        // 归一化表名：去除包裹符号，统一小写
        String normalizedTableName = normalizeTableName(tableName);
        Boolean ignore = ignoredTableCache.get(normalizedTableName);
        if (ignore == null) {
            ignore = computeIgnoreTable(normalizedTableName);
            ignoredTableCache.put(normalizedTableName, ignore);
        }
        return ignore;
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
        if (participateByDefault) {
            // 口径翻转：默认参与隔离，显式声明才忽略
            return false;
        }
        return !TenantEntity.class.isAssignableFrom(entityType);
    }

}
