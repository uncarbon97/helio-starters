package cc.uncarbon.framework.tenant.tenantdatasource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * 数据源级多租户全局 AOP 织入点
 *
 * @author chill
 * @author Uncarbon
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Getter
public class GlobalTenantDataSourceAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    @SuppressWarnings("squid:S1948")
    private final Advice advice;

    @SuppressWarnings("squid:S1948")
    private final Pointcut pointcut;

    public GlobalTenantDataSourceAdvisor(@NonNull GlobalTenantDataSourceInterceptor globalTenantDataSourceInterceptor) {
        this.advice = globalTenantDataSourceInterceptor;
        this.pointcut = buildPointcut();
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }

    /**
     * 配置需要拦截的切面
     */
    private Pointcut buildPointcut() {
        AspectJExpressionPointcut cut = new AspectJExpressionPointcut();
        cut.setExpression(
                "@within(org.springframework.stereotype.Service)"
                + " && !@annotation(cc.uncarbon.framework.tenant.annotation.IgnoreTenantDataSource)"
        );
        return new ComposablePointcut((Pointcut) cut);
    }
}
