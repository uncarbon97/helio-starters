package cc.uncarbon.framework.helium.bizlog.support.aop;

import cc.uncarbon.framework.helium.bizlog.annotation.LogRecord;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 业务日志切点
 * <p>
 * 匹配标注了 {@link LogRecord}（含接口、父类继承）的方法。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public class LogRecordPointcut extends StaticMethodMatcherPointcut implements Serializable {

    @Setter
    private LogRecordOperationSource logRecordOperationSource;

    /**
     * 判断方法是否命中业务日志切点（即能否解析出至少一个 {@code @LogRecord} 操作）。
     *
     * @param method      目标方法
     * @param targetClass 目标类
     * @return 命中返回 {@code true}
     */
    @Override
    public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
        return !CollectionUtils.isEmpty(logRecordOperationSource.computeLogRecordOperations(method, targetClass));
    }
}
