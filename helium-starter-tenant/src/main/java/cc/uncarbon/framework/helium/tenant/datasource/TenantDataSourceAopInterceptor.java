package cc.uncarbon.framework.helium.tenant.datasource;

import cc.uncarbon.framework.helium.db.dynamicdatasource.helper.DynamicDataSourceHelper;
import cc.uncarbon.framework.helium.db.model.DataSourceSetting;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Optional;

/**
 * 数据源级多租户 AOP 拦截器
 *
 * @author chill
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public class TenantDataSourceAopInterceptor implements MethodInterceptor {

    private final DynamicDataSourceHelper dynamicDataSourceHelper;
    private final TenantDataSourceSettingProvider tenantDataSourceSettingProvider;


    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        if (TenantContextHolder.isIgnored()) {
            // 忽略租户，跳过
            return invocation.proceed();
        }

        // 是否有切换过数据源(入栈)
        boolean pushedFlag = false;
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            // 数据源别名，与租户ID相同
            String datasourceAlias = String.valueOf(tenantId);
            boolean switchedFlag = dynamicDataSourceHelper.switchToDataSource(datasourceAlias,
                    () -> getDataSourceSettingOf(tenantId));
            if (switchedFlag) {
                pushedFlag = true;
            }
            return invocation.proceed();
        } finally {
            if (pushedFlag) {
                // 数据源出栈
                DynamicDataSourceContextHolder.poll();
            }
        }
    }

    protected DataSourceSetting getDataSourceSettingOf(Long tenantId) {
        Optional<DataSourceSetting> op = tenantDataSourceSettingProvider.getByTenantId(tenantId);
        return op.orElse(null);
    }
}
