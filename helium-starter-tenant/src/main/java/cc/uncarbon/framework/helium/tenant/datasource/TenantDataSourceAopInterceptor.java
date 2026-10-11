package cc.uncarbon.framework.helium.tenant.datasource;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.db.dynamicdatasource.helper.DynamicDataSourceHelper;
import cc.uncarbon.framework.helium.db.model.DataSourceSetting;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
import cc.uncarbon.framework.helium.tenant.props.HeliumTenantProperties;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private final HeliumTenantProperties props;

    /**
     * 回落 primary 的 error 日志节流（键=维度+方法）：首条全量 error，后续降 debug，
     * 避免登录前/匿名端点高频触发时 ERROR 洪泛掩盖真实故障
     */
    private final Set<String> fallbackErrorLoggedOnce = ConcurrentHashMap.newKeySet();


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
            // null 或非正数（如上游解析出未初始化的 0）都视为缺失，避免以 0 为别名路由数据源
            if (tenantId == null || tenantId <= 0) {
                if (props.isStrict()) {
                    // 响亮失败优于静默落回 primary 共享库（子线程漏传上下文、登录前请求等场景）
                    throw new IllegalStateException("当前线程缺少租户上下文，且已开启严格模式("
                            + ConfigurationPropertiesPrefix.TENANT + ".strict)");
                }
                if (props.isRejectOnFallback()) {
                    throw new IllegalStateException("当前线程缺少租户上下文，且已开启回落即拒绝模式("
                            + ConfigurationPropertiesPrefix.TENANT + ".reject-on-fallback)");
                }
                // 非严格模式保持兼容：在 primary 数据源上执行；error 级留痕，避免跨租户数据错读错写无迹可查
                logFallbackErrorOnce("missing:" + invocation.getMethod(),
                        "当前线程缺少租户上下文，非严格模式回落 primary 共享库执行: " + invocation.getMethod());
                return invocation.proceed();
            }

            // 数据源别名，与租户ID相同
            String datasourceAlias = String.valueOf(tenantId);
            boolean switchedFlag = dynamicDataSourceHelper.switchToDataSource(datasourceAlias,
                    () -> getDataSourceSettingOf(tenantId));
            if (switchedFlag) {
                pushedFlag = true;
            } else if (props.isStrict()) {
                // 租户未配置数据源且动态创建失败，响亮失败优于静默落回 primary 共享库
                throw new IllegalStateException("租户 [" + tenantId + "] 数据源未注册且无法动态创建，且已开启严格模式("
                        + ConfigurationPropertiesPrefix.TENANT + ".strict)");
            } else if (props.isRejectOnFallback()) {
                throw new IllegalStateException("租户 [" + tenantId + "] 数据源未注册且无法动态创建，且已开启回落即拒绝模式("
                        + ConfigurationPropertiesPrefix.TENANT + ".reject-on-fallback)");
            } else {
                // 非严格模式保持兼容：在 primary 数据源上执行；error 级留痕，避免跨租户数据错读错写无迹可查
                logFallbackErrorOnce("switch:" + tenantId + ":" + invocation.getMethod(),
                        "租户 [" + tenantId + "] 数据源未注册且动态创建失败，非严格模式回落 primary 共享库执行: "
                                + invocation.getMethod());
            }
            return invocation.proceed();
        } finally {
            if (pushedFlag) {
                // 数据源出栈
                DynamicDataSourceContextHolder.poll();
            }
        }
    }

    /**
     * 回落 primary 的日志节流：同一维度+方法首条打全量 error，其余降 debug
     */
    protected void logFallbackErrorOnce(String throttleKey, String message) {
        if (fallbackErrorLoggedOnce.add(throttleKey)) {
            log.error("[Framework][数据源级多租户] {}", message);
        } else {
            log.debug("[Framework][数据源级多租户] {}", message);
        }
    }

    protected DataSourceSetting getDataSourceSettingOf(Long tenantId) {
        Optional<DataSourceSetting> op = tenantDataSourceSettingProvider.getByTenantId(tenantId);
        return op.orElse(null);
    }
}
