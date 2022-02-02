package cc.uncarbon.framework.crud.handler;

import cc.uncarbon.framework.core.constant.HelioConstant;
import cc.uncarbon.framework.core.context.UserContextHolder;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.AllArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.List;

/**
 * 行级租户拦截器
 * @author Uncarbon
 */
@AllArgsConstructor
public class HelioTenantLineHandler implements TenantLineHandler {

    /**
     * 忽略行级租户拦截器的表
     */
    private List<String> ignoredTables;

    @Override
    public Expression getTenantId() {
        return new LongValue(UserContextHolder.getRelationalTenant().getTenantId());
    }

    @Override
    public String getTenantIdColumn() {
        return HelioConstant.CRUD.COLUMN_TENANT_ID;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (HelioConstant.CRUD.PRIVILEGED_TENANT_ID.equals(UserContextHolder.getRelationalTenant().getTenantId())) {
            return true;
        }

        return ignoredTables.contains(tableName);
    }

}
