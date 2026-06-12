package cc.uncarbon.framework.helium.tenant.context;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * 租户上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class TenantContextHolder {

    private static final ScopedValue<TenantContext> SCOPED = ScopedValue.newInstance();
    private static final ScopedValue<Boolean> IGNORED = ScopedValue.newInstance();

    @NonNull
    public ScopedValue<TenantContext> scoped() {
        return SCOPED;
    }

    /**
     * 获取当前租户上下文
     */
    @Nullable
    public TenantContext getContext() {
        if (SCOPED.isBound()) {
            return SCOPED.get();
        }
        return null;
    }

    /**
     * 获取当前租户上下文的 {@link Optional} 形式
     */
    @NonNull
    public Optional<TenantContext> getContextOptional() {
        return Optional.ofNullable(getContext());
    }

    /**
     * 快速取当前租户ID
     */
    @Nullable
    public Long getTenantId() {
        TenantContext context = getContext();
        return context == null ? null : context.getTenantId();
    }

    /**
     * 快速取当前租户名称
     */
    @Nullable
    public String getTenantName() {
        TenantContext context = getContext();
        return context == null ? null : context.getTenantName();
    }

    /**
     * 快速取当前租户编码
     */
    @Nullable
    public String getTenantCode() {
        TenantContext context = getContext();
        return context == null ? null : context.getTenantCode();
    }

    /**
     * 是否忽略租户
     */
    public boolean isIgnored() {
        if (IGNORED.isBound()) {
            return IGNORED.get();
        }
        return false;
    }

    /**
     * 在该作用域内忽略租户隔离，结束自动恢复
     */
    public void runIgnored(Runnable op) {
        ScopedValue.where(IGNORED, Boolean.TRUE).run(op);
    }

    /**
     * 在该作用域内忽略租户隔离，结束自动恢复
     */
    public <T> T callIgnored(Callable<T> op) throws Exception {
        return ScopedValue.where(IGNORED, Boolean.TRUE).call(op::call);
    }
}
