package cc.uncarbon.framework.helium.tenant.aop;

import cc.uncarbon.framework.helium.tenant.annotation.TenantIgnore;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 租户忽略注解切面
 *
 * @author Charles7c@continew
 * @author Uncarbon
 */
@Aspect
public class TenantIgnoreAspect {

    /**
     * 忽略租户
     * <p>结果槽 + 异常槽模式：完整透传 {@link Throwable}（含受检异常与 Error），
     * 不改变异常类型，避免方法签名声明 {@code throws IOException} 等受检异常被包成裸 RuntimeException
     * 导致调用方按原类型 catch 不到。</p>
     *
     * @param joinPoint 切点
     * @return 返回结果
     */
    @Around("@annotation(tenantIgnore) || @within(tenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint, TenantIgnore tenantIgnore) throws Throwable {
        boolean oldVal = TenantContextHolder.isIgnored();
        if (oldVal) {
            return joinPoint.proceed();
        }
        final Object[] result = new Object[1];
        final Throwable[] error = new Throwable[1];
        TenantContextHolder.runIgnored(() -> {
            try {
                result[0] = joinPoint.proceed();
            } catch (Throwable t) {
                error[0] = t;
            }
        });
        if (error[0] != null) {
            throw error[0];
        }
        return result[0];
    }
}
