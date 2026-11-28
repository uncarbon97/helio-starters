package cc.uncarbon.framework.helium.bizlog.support.parse;

import cc.uncarbon.framework.helium.bizlog.service.IFunctionService;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * 自定义函数解析器
 * <p>
 * 负责在模板渲染时调用对应的 {@link IFunctionService}，并复用前置函数的缓存结果。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@AllArgsConstructor
public class LogFunctionParser {

    @Setter
    private IFunctionService functionService;


    /**
     * 取自定义函数的返回值：优先复用前置阶段已缓存的结果，否则实时调用函数服务。
     *
     * @param beforeFunctionNameAndReturnMap 前置函数返回值缓存
     * @param value                          SpEL 求值后的入参
     * @param expression                     原始内层表达式
     * @param functionName                   函数名（为空时直接返回入参字符串）
     * @return 函数返回的文案
     */
    public String getFunctionReturnValue(Map<String, String> beforeFunctionNameAndReturnMap, Object value, String expression, String functionName) {
        if (ObjectUtils.isEmpty(functionName)) {
            return value == null ? "" : value.toString();
        }
        String functionReturnValue;
        String functionCallInstanceKey = getFunctionCallInstanceKey(functionName, expression);
        if (beforeFunctionNameAndReturnMap != null && beforeFunctionNameAndReturnMap.containsKey(functionCallInstanceKey)) {
            functionReturnValue = beforeFunctionNameAndReturnMap.get(functionCallInstanceKey);
        } else {
            functionReturnValue = functionService.apply(functionName, value);
        }
        return functionReturnValue;
    }

    /**
     * 取函数调用的缓存 key。
     * <p>
     * 方法执行前会缓存前置函数的结果，此时函数调用的唯一标志为：函数名 + 参数表达式。
     *
     * @param functionName    函数名
     * @param paramExpression 解析前的内层表达式
     * @return 缓存 key
     */
    public String getFunctionCallInstanceKey(String functionName, String paramExpression) {
        return functionName + paramExpression;
    }

    /**
     * 判断函数是否前置执行。
     *
     * @param functionName 函数名
     * @return 前置执行返回 {@code true}
     */
    public boolean beforeFunction(String functionName) {
        return functionService.beforeFunction(functionName);
    }
}
