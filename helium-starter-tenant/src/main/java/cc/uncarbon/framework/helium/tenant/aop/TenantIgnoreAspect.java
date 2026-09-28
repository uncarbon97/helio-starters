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
        return TenantContextHolder.callIgnored(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                // RuntimeException 原样放行，避免 BusinessException 等被二次包装成裸 RuntimeException；Error 不在此捕获
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException(e);
            }
        });
    }
}
