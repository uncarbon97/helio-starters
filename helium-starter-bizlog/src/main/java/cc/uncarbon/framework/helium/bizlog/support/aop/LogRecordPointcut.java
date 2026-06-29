package cc.uncarbon.framework.helium.bizlog.support.aop;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 业务日志切点
 * <p>
 * 匹配标注了 {@code @LogRecord}（含接口、父类继承）的方法。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public class LogRecordPointcut extends StaticMethodMatcherPointcut implements Serializable {


    private LogRecordOperationSource logRecordOperationSource;

    /**
     * 判断方法是否命中业务日志切点（即能否解析出至少一个 {@code @LogRecord} 操作）。
     *
     * @param method      目标方法
     * @param targetClass 目标类
     * @return 命中返回 {@code true}
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return !CollectionUtils.isEmpty(logRecordOperationSource.computeLogRecordOperations(method, targetClass));
    }

    /**
     * 注入注解解析器。
     *
     * @param logRecordOperationSource 注解解析器
     */
    void setLogRecordOperationSource(LogRecordOperationSource logRecordOperationSource) {
        this.logRecordOperationSource = logRecordOperationSource;
    }
}
