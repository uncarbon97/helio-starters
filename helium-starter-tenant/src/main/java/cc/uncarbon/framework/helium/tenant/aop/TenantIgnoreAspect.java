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
    @Around("@annotation(tenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint, TenantIgnore tenantIgnore) throws Throwable {
        boolean oldVal = TenantContextHolder.isIgnored();
        if (oldVal) {
            return joinPoint.proceed();
        }
        return TenantContextHolder.callIgnored(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }
}
