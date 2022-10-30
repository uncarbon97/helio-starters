package cc.uncarbon.framework.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import java.util.Optional;
import lombok.experimental.UtilityClass;

/**
 * 租户上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class TenantContextHolder {

    private final TransmittableThreadLocal<TenantContext> THREAD_LOCAL_CONTEXT = new TransmittableThreadLocal<>();


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
    public synchronized void setTenantContext(TenantContext newContext) {
        if (newContext == null) {
            THREAD_LOCAL_CONTEXT.remove();
            return;
        }

        THREAD_LOCAL_CONTEXT.set(newContext);
    }

    /**
     * 强制清空本线程的租户上下文，防止影响被线程池复用的其他线程，以及内存泄露
     */
    public void clear() {
        setTenantContext(null);
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
     * 捷径API-取当前租户名
     *
     * @return null or 当前租户名
     */
    public String getTenantName() {
        TenantContext context = getTenantContext();
        return context == null ? null : context.getTenantName();
    }
}
