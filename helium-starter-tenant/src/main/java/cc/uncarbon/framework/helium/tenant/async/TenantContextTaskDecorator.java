package cc.uncarbon.framework.helium.tenant.async;

import cc.uncarbon.framework.helium.tenant.context.TenantContext;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
import org.jspecify.annotations.NonNull;
import org.springframework.core.task.TaskDecorator;

/**
 * 租户上下文任务装饰器
 *
 * <p>在任务提交时捕获当前线程的 {@link TenantContext} 快照与忽略租户标记，
 * 在子线程执行前恢复、执行后自动释放，供 @Async / 自建线程池 / 定时任务统一包装。</p>
 *
 * <p>虚拟线程与结构化并发（StructuredTaskScope）下 ScopedValue 子任务天然继承，无需本装饰器；
 * 本类仅兜底「不继承」的场景（如 Spring @Async 泛型线程池、自建 ExecutorService）。</p>
 *
 * @author Uncarbon
 */
public class TenantContextTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        // 提交时刻快照
        TenantContext snapshotContext = TenantContextHolder.getContext();
        boolean snapshotIgnored = TenantContextHolder.isIgnored();

        return () -> {
            // 子线程内恢复快照，作用域结束自动释放（ScopedValue 语义，嵌套安全）
            ScopedValue.where(TenantContextHolder.scoped(), snapshotContext)
                    .where(TenantContextHolder.ignoredScoped(), snapshotIgnored)
                    .run(runnable);
        };
    }
}
