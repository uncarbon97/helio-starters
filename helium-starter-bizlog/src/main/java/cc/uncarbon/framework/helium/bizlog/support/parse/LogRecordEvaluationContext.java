package cc.uncarbon.framework.helium.bizlog.support.parse;

import cc.uncarbon.framework.helium.bizlog.context.LogRecordContext;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 日志模板 SpEL 求值上下文
 * <p>
 * 继承 {@link MethodBasedEvaluationContext}，在方法参数基础上注入：
 * 当前栈帧变量、全局变量（不覆盖同名方法级变量）、方法返回值 {@code _ret}、异常消息 {@code _errorMsg}。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public class LogRecordEvaluationContext extends MethodBasedEvaluationContext {

    /**
     * 构造 SpEL 求值上下文。
     *
     * @param rootObject              根对象
     * @param method                  目标方法
     * @param arguments               方法参数
     * @param parameterNameDiscoverer 参数名发现器
     * @param ret                     方法返回值，注入为 {@code _ret}
     * @param errorMsg                异常消息，注入为 {@code _errorMsg}
     */
    public LogRecordEvaluationContext(Object rootObject, Method method, Object[] arguments,
                                      ParameterNameDiscoverer parameterNameDiscoverer, Object ret, String errorMsg) {
        super(rootObject, method, arguments, parameterNameDiscoverer);
        Map<String, Object> variables = LogRecordContext.getVariables();
        Map<String, Object> globalVariable = LogRecordContext.getGlobalVariableMap();
        if (variables != null) {
            setVariables(variables);
        }
        if (globalVariable != null && !globalVariable.isEmpty()) {
            for (Map.Entry<String, Object> entry : globalVariable.entrySet()) {
                // 方法级变量优先，全局变量不覆盖同名方法级变量
                if (lookupVariable(entry.getKey()) == null) {
                    setVariable(entry.getKey(), entry.getValue());
                }
            }
        }
        setVariable("_ret", ret);
        setVariable("_errorMsg", errorMsg);
    }
}
