package cc.uncarbon.framework.helium.tenant.line;

import cc.uncarbon.framework.helium.db.constant.EntityField;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;

import java.util.Collection;

@RequiredArgsConstructor
public class DefaultTenantLineHandler implements TenantLineHandler {

    /**
     * 忽略租户隔离的表
     */
    private final Collection<String> ignoredTables;


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
        // 忽略租户
        if (TenantContextHolder.isIgnored()) {
            return true;
        }
        return ignoredTables.contains(tableName);
    }

}