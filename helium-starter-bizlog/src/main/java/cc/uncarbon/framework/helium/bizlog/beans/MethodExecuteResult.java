package cc.uncarbon.framework.helium.bizlog.beans;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

/**
 * 方法执行结果
 * <p>
 * 封装目标方法的执行状态（成功/失败）、异常、返回值及方法元信息，供日志记录流程使用。
 *
 * @author wulang@mzt-biz-log
 * @author Uncarbon
 **/
@Getter
public class MethodExecuteResult {
    /**
     * 是否执行成功
     */
    @Setter
    private boolean success;
    /**
     * 执行抛出的异常
     */
    @Setter
    private Throwable throwable;
    /**
     * 异常消息
     */
    @Setter
    private String errorMsg;

    /**
     * 方法返回值
     */
    @Setter
    private Object result;
    /**
     * 目标方法
     */
    private final Method method;
    /**
     * 方法参数
     */
    private final Object[] args;
    /**
     * 目标类
     */
    private final Class<?> targetClass;

    /**
     * 构造方法执行结果，记录方法元信息。
     *
     * @param method      目标方法
     * @param args        方法参数
     * @param targetClass 目标类
     */
    public MethodExecuteResult(Method method, Object[] args, Class<?> targetClass) {
        this.method = method;
        this.args = args;
        this.targetClass = targetClass;
    }
}
