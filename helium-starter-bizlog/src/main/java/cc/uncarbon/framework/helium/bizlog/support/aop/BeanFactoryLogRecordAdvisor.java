package cc.uncarbon.framework.helium.bizlog.support.aop;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * 业务日志切面
 * <p>
 * 组合 {@link LogRecordPointcut}（匹配 {@code @LogRecord} 方法）与日志拦截器，
 * 供 Spring 据此为命中 Bean 创建代理。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public class BeanFactoryLogRecordAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private final LogRecordPointcut pointcut = new LogRecordPointcut();

    /**
     * 返回切点。
     *
     * @return 业务日志切点
     */
    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    /**
     * 注入注解解析器（透传给切点）。
     *
     * @param logRecordOperationSource 注解解析器
     */
    public void setLogRecordOperationSource(LogRecordOperationSource logRecordOperationSource) {
        pointcut.setLogRecordOperationSource(logRecordOperationSource);
    }
}
