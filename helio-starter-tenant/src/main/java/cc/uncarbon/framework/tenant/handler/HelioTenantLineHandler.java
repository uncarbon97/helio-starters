package cc.uncarbon.framework.tenant.handler;

import cc.uncarbon.framework.core.constant.HelioConstant;
import cc.uncarbon.framework.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import java.util.List;
import lombok.AllArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

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
        return new LongValue(TenantContextHolder.getTenantContext().getTenantId());
    }

    @Override
    public String getTenantIdColumn() {
        return HelioConstant.CRUD.COLUMN_TENANT_ID;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (HelioConstant.CRUD.PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantContext().getTenantId())) {
            return true;
        }

        return ignoredTables.contains(tableName);
    }

}
