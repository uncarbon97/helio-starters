package cc.uncarbon.framework.helium.tenant.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * 租户上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class TenantContextHolder {

    private static final TransmittableThreadLocal<TenantContext> THREAD_LOCAL_CONTEXT = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<Boolean> THREAD_LOCAL_IGNORED = new TransmittableThreadLocal<>();


    /**
     * 强制清空本线程的租户上下文，防止影响被线程池复用的其他线程，以及内存泄露
     */
    public void clear() {
        setTenantContext(null);
        setIgnored(false);
    }

    /**
     * 获取当前租户上下文
     *
     * @return null or 当前租户上下文
     */
    public TenantContext getTenantContext() {
        return THREAD_LOCAL_CONTEXT.get();
    }

    /**
     * 获取当前租户上下文
     */
    public Optional<TenantContext> getTenantContextOptional() {
        return Optional.ofNullable(THREAD_LOCAL_CONTEXT.get());
    }

    /**
     * 设置当前租户上下文
     *
     * @param newContext 新上下文，传 null 则为清除
     */
    public void setTenantContext(TenantContext newContext) {
        if (newContext == null) {
            THREAD_LOCAL_CONTEXT.remove();
            return;
        }

        THREAD_LOCAL_CONTEXT.set(newContext);
    }

    /**
     * 设置是否忽略租户
     *
     * @param ignored 是否忽略租户
     */
    public void setIgnored(boolean ignored) {
        THREAD_LOCAL_IGNORED.set(ignored);
    }

    /**
     * 是否忽略租户
     *
     * @return 是否忽略租户
     */
    public boolean isIgnored() {
        return Boolean.TRUE.equals(THREAD_LOCAL_IGNORED.get());
    }

    /**
     * 捷径API-取当前租户ID
     *
     * @return null or 当前租户ID
     */
    public Long getTenantId() {
        TenantContext context = getTenantContext();
        return context == null ? null : context.getTenantId();
    }

    /**
     * 捷径API-取当前租户名称
     *
     * @return null or 当前租户名称
     */
    public String getTenantName() {
        TenantContext context = getTenantContext();
        return context == null ? null : context.getTenantName();
    }

    /**
     * 捷径API-取当前租户编码
     *
     * @return null or 当前租户编码
     */
    public String getTenantCode() {
        TenantContext context = getTenantContext();
        return context == null ? null : context.getTenantCode();
    }
}
