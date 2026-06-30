package cc.uncarbon.framework.helium.bizlog.support.parse;

import cc.uncarbon.framework.helium.bizlog.model.MethodExecuteResult;
import cc.uncarbon.framework.helium.bizlog.service.impl.DiffParseFunction;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志模板解析器
 * <p>
 * 解析需要存储的日志文案中的 SpEL 表达式与自定义函数占位符 {@code {函数名{SpEL}}}，
 * 同时支持 diff 函数 {@code _DIFF} 的渲染
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public class LogRecordValueParser implements BeanFactoryAware {

    /**
     * 匹配 {@code {函数名{表达式}}} 占位符的正则
     */
    private static final Pattern pattern = Pattern.compile("\\{\\s*(\\w*)\\s*\\{(.*?)}}");
    public static final String COMMA = ",";
    private final LogRecordExpressionEvaluator expressionEvaluator = new LogRecordExpressionEvaluator();
    protected BeanFactory beanFactory;
    protected boolean diffSameWhetherSaveLog;

    @Setter
    private LogFunctionParser logFunctionParser;
    @Setter
    private DiffParseFunction diffParseFunction;

    /**
     * 统计子串在源串中出现的次数。
     *
     * @param srcText  源串
     * @param findText 待统计子串
     * @return 出现次数
     */
    public static int strCount(String srcText, String findText) {
        int count = 0;
        int index = 0;
        while ((index = srcText.indexOf(findText, index)) != -1) {
            index = index + findText.length();
            count++;
        }
        return count;
    }

    /**
     * 解析单个模板并返回其渲染结果。
     *
     * @param methodExecuteResult            方法执行结果
     * @param templates                      单个模板文本
     * @param beforeFunctionNameAndReturnMap 前置函数返回值缓存
     * @return 模板渲染后的文案
     */
    public String singleProcessTemplate(MethodExecuteResult methodExecuteResult,
                                        String templates,
                                        Map<String, String> beforeFunctionNameAndReturnMap) {
        Map<String, String> stringStringMap = processTemplate(Collections.singletonList(templates), methodExecuteResult,
                beforeFunctionNameAndReturnMap);
        return stringStringMap.get(templates);
    }

    /**
     * 批量解析模板，逐个渲染 SpEL 与自定义函数占位符，并处理 diff 相同时是否保留原模板。
     *
     * @param templates                      模板集合
     * @param methodExecuteResult            方法执行结果
     * @param beforeFunctionNameAndReturnMap 前置函数返回值缓存
     * @return 模板到渲染结果的映射
     */
    public Map<String, String> processTemplate(Collection<String> templates, MethodExecuteResult methodExecuteResult,
                                               Map<String, String> beforeFunctionNameAndReturnMap) {
        Map<String, String> expressionValues = new HashMap<>();
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(methodExecuteResult.getMethod(),
                methodExecuteResult.getArgs(), methodExecuteResult.getTargetClass(), methodExecuteResult.getResult(),
                methodExecuteResult.getErrorMsg(), beanFactory);

        for (String expressionTemplate : templates) {
            if (expressionTemplate.contains("{")) {
                Matcher matcher = pattern.matcher(expressionTemplate);
                StringBuilder parsedStr = new StringBuilder();
                AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(methodExecuteResult.getMethod(), methodExecuteResult.getTargetClass());
                boolean sameDiff = false;
                while (matcher.find()) {
                    String expression = matcher.group(2);
                    String functionName = matcher.group(1);
                    if (DiffParseFunction.diffFunctionName.equals(functionName)) {
                        expression = getDiffFunctionValue(evaluationContext, annotatedElementKey, expression);
                        sameDiff = Objects.equals("", expression);
                    } else {
                        Object value = expressionEvaluator.parseExpression(expression, annotatedElementKey, evaluationContext);
                        expression = logFunctionParser.getFunctionReturnValue(beforeFunctionNameAndReturnMap, value, expression, functionName);
                    }
                    matcher.appendReplacement(parsedStr, Matcher.quoteReplacement(expression == null ? "" : expression));
                }
                matcher.appendTail(parsedStr);
                expressionValues.put(expressionTemplate, recordSameDiff(sameDiff, diffSameWhetherSaveLog) ? parsedStr.toString() : expressionTemplate);
            } else {
                expressionValues.put(expressionTemplate, expressionTemplate);
            }

        }
        return expressionValues;
    }

    /**
     * 判断 diff 结果是否需要记录：配置「全部记录」时恒为真；否则 diff 无变化时不记录。
     *
     * @param sameDiff               diff 是否无变化
     * @param diffSameWhetherSaveLog 是否不校验文案、全部记录
     * @return 是否记录
     */
    private boolean recordSameDiff(boolean sameDiff, boolean diffSameWhetherSaveLog) {
        if (diffSameWhetherSaveLog) {
            return true;
        }
        return !sameDiff;
    }

    /**
     * 解析 diff 函数占位符：按参数个数（1 或 2）调用单参/双参 diff。
     *
     * @param evaluationContext   求值上下文
     * @param annotatedElementKey 方法键
     * @param expression          占位符内层表达式
     * @return diff 文案
     */
    private String getDiffFunctionValue(EvaluationContext evaluationContext, AnnotatedElementKey annotatedElementKey, String expression) {
        String[] params = parseDiffFunction(expression);
        if (params.length == 1) {
            Object targetObj = expressionEvaluator.parseExpression(params[0], annotatedElementKey, evaluationContext);
            expression = diffParseFunction.diff(targetObj);
        } else if (params.length == 2) {
            Object sourceObj = expressionEvaluator.parseExpression(params[0], annotatedElementKey, evaluationContext);
            Object targetObj = expressionEvaluator.parseExpression(params[1], annotatedElementKey, evaluationContext);
            expression = diffParseFunction.diff(sourceObj, targetObj);
        }
        return expression;
    }

    /**
     * 解析 diff 内层表达式参数：仅当恰好包含一个逗号时拆分为两个参数，否则视为单参。
     *
     * @param expression 内层表达式
     * @return 参数数组
     */
    private String[] parseDiffFunction(String expression) {
        if (expression.contains(COMMA) && strCount(expression, COMMA) == 1) {
            return expression.split(COMMA);
        }
        return new String[]{expression};
    }

    /**
     * 解析并执行模板中的前置函数（在业务方法执行前调用），缓存其返回值。
     *
     * @param templates   前置函数模板集合
     * @param targetClass 目标类
     * @param method      目标方法
     * @param args        方法参数
     * @return 函数调用 key 到返回值的映射
     */
    public Map<String, String> processBeforeExecuteFunctionTemplate(Collection<String> templates, Class<?> targetClass, Method method, Object[] args) {
        Map<String, String> functionNameAndReturnValueMap = new HashMap<>();
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(method, args, targetClass, null, null, beanFactory);

        for (String expressionTemplate : templates) {
            if (expressionTemplate.contains("{")) {
                Matcher matcher = pattern.matcher(expressionTemplate);
                while (matcher.find()) {
                    String expression = matcher.group(2);
                    if (expression.contains("#_ret") || expression.contains("#_errorMsg")) {
                        continue;
                    }
                    AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(method, targetClass);
                    String functionName = matcher.group(1);
                    if (logFunctionParser.beforeFunction(functionName)) {
                        Object value = expressionEvaluator.parseExpression(expression, annotatedElementKey, evaluationContext);
                        String functionReturnValue = logFunctionParser.getFunctionReturnValue(null, value, expression, functionName);
                        String functionCallInstanceKey = logFunctionParser.getFunctionCallInstanceKey(functionName, expression);
                        functionNameAndReturnValueMap.put(functionCallInstanceKey, functionReturnValue);
                    }
                }
            }
        }
        return functionNameAndReturnValueMap;
    }


    /**
     * 注入 Bean 工厂。
     *
     * @param beanFactory Bean 工厂
     * @throws BeansException 异常
     */
    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
