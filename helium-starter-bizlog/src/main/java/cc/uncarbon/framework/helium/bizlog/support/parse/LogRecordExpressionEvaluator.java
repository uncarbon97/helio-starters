package cc.uncarbon.framework.helium.bizlog.support.parse;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpEL 表达式求值器
 * <p>
 * 编译并缓存 {@link Expression}，结合 {@link LogRecordEvaluationContext} 对模板中的 SpEL 片段求值。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public class LogRecordExpressionEvaluator extends CachedExpressionEvaluator {

    private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);
    private final Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(64);

    /**
     * 解析单个 SpEL 表达式。
     *
     * @param conditionExpression 表达式文本
     * @param methodKey           方法键（用于缓存）
     * @param evalContext         求值上下文
     * @return 求值结果
     */
    public Object parseExpression(String conditionExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return getExpression(this.expressionCache, methodKey, conditionExpression).getValue(evalContext, Object.class);
    }

    /**
     * 构造 SpEL 求值上下文。
     *
     * @param method      目标方法
     * @param args        方法参数
     * @param targetClass 目标类
     * @param result      方法返回值（可为 {@code null}）
     * @param errorMsg    异常消息
     * @param beanFactory Spring Bean 工厂（用于支持 SpEL 中按名引用 Bean，可为 {@code null}）
     * @return 求值上下文
     */
    public EvaluationContext createEvaluationContext(Method method, Object[] args, Class<?> targetClass,
                                                     Object result, String errorMsg, BeanFactory beanFactory) {
        Method targetMethod = getTargetMethod(targetClass, method);
        LogRecordEvaluationContext evaluationContext = new LogRecordEvaluationContext(
                null, targetMethod, args, getParameterNameDiscoverer(), result, errorMsg);
        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }

    /**
     * 取目标类上最具体的方法（带缓存）。
     *
     * @param targetClass 目标类
     * @param method      方法
     * @return 最具体的方法
     */
    private Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        return targetMethodCache.computeIfAbsent(methodKey, _ -> AopUtils.getMostSpecificMethod(method, targetClass));
    }
}
