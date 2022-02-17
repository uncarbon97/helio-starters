package cc.uncarbon.framework.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import java.util.Optional;
import lombok.experimental.UtilityClass;

/**
 * 存储当前租户上下文
 *
 * @author Uncarbon
 */
@UtilityClass
public class TenantContextHolder {

    private final TransmittableThreadLocal<TenantContext> THREAD_LOCAL_TENANT = new TransmittableThreadLocal<>();


    /**
     * 获取当前用户上下文
     * 注意：即使未登录情况下，该方法也不会返回 null ，而是返回一个空对象
     *
     * @return 当前用户上下文
     */
    public TenantContext getTenantContext() {
        /*
        必须新创建，否则可能出现租户ID无法切换的BUG
        不能用 .orElse ，即使条件不满足也会new
         */
        return Optional.ofNullable(THREAD_LOCAL_TENANT.get()).orElseGet(TenantContext::new);
    }

    /**
     * 设置当前用户上下文
     *
     * @param tenantContext 当前用户上下文
     */
    public void setTenantContext(TenantContext tenantContext) {
        if (tenantContext == null) {
            THREAD_LOCAL_TENANT.remove();
            return;
        }

        THREAD_LOCAL_TENANT.set(tenantContext);
    }

}
