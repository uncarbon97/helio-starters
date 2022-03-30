package cc.uncarbon.framework.tenant.tenantdatasource;

import cc.uncarbon.framework.core.constant.HelioConstant.CRUD;
import cc.uncarbon.framework.core.context.TenantContextHolder;
import cc.uncarbon.framework.crud.dynamicdatasource.AbstractDataSourceRegistry;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 数据源级多租户全局 AOP 处理过程
 *
 * @author chill
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public class GlobalTenantDataSourceInterceptor implements MethodInterceptor {

    private final AbstractDataSourceRegistry dataSourceRegistry;


    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        Long currentTenantId = TenantContextHolder.getTenantId();
        if (Objects.isNull(currentTenantId)) {
            // 没有租户信息，直接跳过
            return invocation.proceed();
        }

        try {
            String tenantDataSourceName = CRUD.COLUMN_TENANT_ID + currentTenantId;
            dataSourceRegistry.registerDataSource(tenantDataSourceName);

            log.debug("[多租户][数据源级]切换到租户 >> {}", tenantDataSourceName);
            DynamicDataSourceContextHolder.push(tenantDataSourceName);
            return invocation.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }
}
