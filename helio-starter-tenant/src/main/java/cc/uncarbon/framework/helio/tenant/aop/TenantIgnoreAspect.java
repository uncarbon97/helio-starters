package cc.uncarbon.framework.helio.tenant.aop;

import cc.uncarbon.framework.helio.tenant.annotation.TenantIgnore;
import cc.uncarbon.framework.helio.tenant.context.TenantContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 租户忽略注解切面
 *
 * @author Charles7c
 */
@Aspect
public class TenantIgnoreAspect {

    /**
     * 忽略租户
     *
     * @param joinPoint 切点
     * @return 返回结果
     * @throws Throwable 异常
     */
    @Around("@annotation(tenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint, TenantIgnore tenantIgnore) throws Throwable {
        boolean oldVal = TenantContextHolder.isIgnored();
        if (oldVal) {
            return joinPoint.proceed();
        }
        try {
            TenantContextHolder.setIgnored(true);
            return joinPoint.proceed();
        } finally {
            TenantContextHolder.setIgnored(false);
        }
    }
}
