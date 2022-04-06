package cc.uncarbon.framework.tenant.tenantdatasource;

import cc.uncarbon.framework.core.constant.HelioConstant.CRUD;
import cc.uncarbon.framework.core.context.TenantContextHolder;
import cc.uncarbon.framework.crud.dynamicdatasource.HelioDynamicDataSourceRegistry;
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

    private final HelioDynamicDataSourceRegistry dataSourceRegistry;


    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        Long currentTenantId = TenantContextHolder.getTenantId();
        if (Objects.isNull(currentTenantId)) {
            // 没有租户信息，直接跳过
            return invocation.proceed();
        }

        try {
            // 不适合纯数字作为数据源名称，给他拼个前缀
            String tenantDataSourceName = CRUD.COLUMN_TENANT_ID + currentTenantId;
            if (dataSourceRegistry.containsDataSource(tenantDataSourceName, true)) {
                log.debug("[多租户][数据源级] 使用租户数据源 >> {}", tenantDataSourceName);
                DynamicDataSourceContextHolder.push(tenantDataSourceName);
            }

            return invocation.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }
}
