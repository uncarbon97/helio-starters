package cc.uncarbon.framework.helium.tenant.line;

import cc.uncarbon.framework.helium.db.constant.EntityField;
import cc.uncarbon.framework.helium.db.entity.TenantEntity;
import cc.uncarbon.framework.helium.tenant.annotation.TenantIgnore;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
import cc.uncarbon.framework.helium.tenant.props.HeliumTenantProperties;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.schema.Column;

import java.util.Collection;
import java.util.List;
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
@Slf4j
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
     * 非严格模式：上下文缺租户时的兜底租户 ID
     */
    private final Long nonStrictFallbackTenantId;

    /**
     * 表名是否忽略租户的计算结果缓存（表名统一小写作为键）
     */
    private final Map<String, Boolean> ignoredTableCache = new ConcurrentHashMap<>();


    public DefaultTenantLineHandler(HeliumTenantProperties props) {
        this(props.getIgnoredTables(), props.isParticipateByDefault(),
                props.isStrict(), props.getNonStrictFallbackTenantId());
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
                // 响亮失败
                throw new IllegalStateException("缺少租户上下文，严格模式下无法继续");
            } else if (nonStrictFallbackTenantId != null) {
                // 兜底写值
                return new LongValue(nonStrictFallbackTenantId);
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
            ignore = computeIgnoreTable(tableName, normalizedTableName);
            ignoredTableCache.put(normalizedTableName, ignore);
        }
        return ignore;
    }

    /**
     * 避免入参携带伪造 tenantId ，造成跨租户写入
     * <p>上下文缺租户：严格模式拒绝；非严格模式警告
     * <p>上下文有租户：{@code MybatisPlusAutoFillColumnHandler#overrideTenantId} 覆盖写入
     */
    @Override
    public boolean ignoreInsert(List<Column> columns, String tenantIdColumn) {
        boolean containsTenantColumn = columns.stream().map(Column::getColumnName)
                .anyMatch(i -> i.equalsIgnoreCase(tenantIdColumn));
        if (containsTenantColumn && !TenantContextHolder.isIgnored()) {
            Long currentTenantId = TenantContextHolder.getTenantId();
            if (currentTenantId == null) {
                if (strict) {
                    // 响亮失败
                    throw new IllegalStateException("存在租户列，但缺少租户上下文，严格模式下拒绝写入");
                }
                log.warn(TenantLineConfiguration.LOG_PREFIX + " 存在租户列，但缺少租户上下文，非严格模式下存在跨租户写入风险 >> columns={}", columns);
            }
        }
        return containsTenantColumn;
    }

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    protected boolean isConfiguredIgnored(String normalizedTableName) {
        if (ignoredTables == null) {
            return false;
        }
        for (String configuredTableName : ignoredTables) {
            // 配置项同样归一化后再比较，避免与运行时口径不一致（如配置带反引号时对账通过、运行时漏匹配）
            if (normalizedTableName.equalsIgnoreCase(normalizeTableName(configuredTableName))) {
                return true;
            }
        }
        return false;
    }

    protected boolean computeIgnoreTable(String originalTableName, String normalizedTableName) {
        // 显式配置忽略的表
        if (isConfiguredIgnored(normalizedTableName)) {
            return true;
        }

        /*
        MP 的 TableInfo 缓存按实体原始表名精确匹配；
        先按原始表名查，miss 再用归一化名兜底，避免混合大小写表名（如 @TableName("SysConfig")）
        被误判为"纯 XML 表"而错误地保持参与隔离
         */
        TableInfo tableInfo = TableInfoHelper.getTableInfo(originalTableName);
        if (tableInfo == null) {
            tableInfo = TableInfoHelper.getTableInfo(normalizedTableName);
        }
        if (tableInfo == null) {
            /*
            大小写残余兜底：XML 手写 SQL 表名与 @TableName 大小写不一致时两次精确查找都会 miss，
            会被误判为「纯 XML 表」而错误参与隔离；全量归一化比对一次
             */
            tableInfo = TableInfoHelper.getTableInfos().stream()
                    .filter(info -> normalizedTableName.equals(normalizeTableName(info.getTableName())))
                    .findFirst()
                    .orElse(null);
        }
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
