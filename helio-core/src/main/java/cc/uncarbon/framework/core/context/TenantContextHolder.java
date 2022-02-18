package cc.uncarbon.framework.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

/**
 * 租户上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class TenantContextHolder {

    private final TransmittableThreadLocal<TenantContext> THREAD_LOCAL_TENANT = new TransmittableThreadLocal<>();
    /**
     * 是否启用多租户功能
     */
    private boolean enableTenant = false;

    /**
     * 获取当前租户上下文
     *
     * @return null or 当前租户上下文
     */
    public TenantContext getTenantContext() {
        return THREAD_LOCAL_TENANT.get();
    }

    /**
     * 设置当前租户上下文
     *
     * @param tenantContext 新上下文，传 null 则为清除
     */
    public void setTenantContext(TenantContext tenantContext) {
        if (tenantContext == null) {
            THREAD_LOCAL_TENANT.remove();
            return;
        }

        THREAD_LOCAL_TENANT.set(tenantContext);
    }

    public boolean isEnableTenant() {
        return enableTenant;
    }

    public void setEnableTenant(boolean enableTenant) {
        TenantContextHolder.enableTenant = enableTenant;
    }
}
