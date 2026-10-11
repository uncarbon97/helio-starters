package cc.uncarbon.framework.helium.bizlog.support.parse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpEL 表达式求值器
 * <p>
 * 编译并缓存 {@link Expression}，结合 {@link LogRecordEvaluationContext} 对模板中的 SpEL 片段求值。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
@Slf4j
public class LogRecordExpressionEvaluator extends CachedExpressionEvaluator {

    protected final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);
    protected final Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(64);
    protected final Set<Method> missingParameterNameWarned = ConcurrentHashMap.newKeySet();


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
        warnIfParameterNamesMissing(targetMethod);
        LogRecordEvaluationContext evaluationContext = new LogRecordEvaluationContext(
                null, targetMethod, args, getParameterNameDiscoverer(), result, errorMsg);
        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }

    /**
     * 参数名不可用时（编译未保留 -parameters），SpEL 中以 #参数名 引用会解析失败，warn 提示一次。
     * <p>继承本工程 parent 的下游已默认开启 -parameters，第三方工程需自行开启。</p>
     */
    protected void warnIfParameterNamesMissing(Method method) {
        if (!missingParameterNameWarned.add(method)) {
            return;
        }
        String[] parameterNames = null;
        try {
            parameterNames = getParameterNameDiscoverer().getParameterNames(method);
        } catch (Exception ignored) {
        }
        if (parameterNames == null) {
            log.warn("[LogRecordExpressionEvaluator] 方法 {} 的参数名不可用（编译需保留 -parameters），模板中以 #参数名 引用将解析失败",
                    method);
        }
    }

    /**
     * 取目标类上最具体的方法（带缓存）。
     *
     * @param targetClass 目标类
     * @param method      方法
     * @return 最具体的方法
     */
    protected Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        return targetMethodCache.computeIfAbsent(methodKey, _ -> AopUtils.getMostSpecificMethod(method, targetClass));
    }
}
