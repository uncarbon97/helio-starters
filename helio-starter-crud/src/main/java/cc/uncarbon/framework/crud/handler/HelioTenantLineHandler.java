package cc.uncarbon.framework.crud.handler;

import cc.uncarbon.framework.core.constant.HelioConstant;
import cc.uncarbon.framework.core.context.UserContextHolder;
import cc.uncarbon.framework.core.props.HelioProperties;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import javax.annotation.Resource;

/**
 * 行级租户拦截器
 * @author Uncarbon
 */
public class HelioTenantLineHandler implements TenantLineHandler {

    @Resource
    private HelioProperties helioProperties;


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

        return helioProperties.getCrud().getTenant().getIgnoredTables().contains(tableName);
    }

}
