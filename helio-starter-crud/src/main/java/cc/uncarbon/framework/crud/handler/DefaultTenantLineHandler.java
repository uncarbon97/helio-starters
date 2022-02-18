package cc.uncarbon.framework.crud.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 默认行级租户拦截器
 *
 * @author Uncarbon
 */
@Component
@ConditionalOnMissingBean(TenantLineHandler.class)
public class DefaultTenantLineHandler implements TenantLineHandler {

    @Override
    public Expression getTenantId() {
        return null;
    }
}
