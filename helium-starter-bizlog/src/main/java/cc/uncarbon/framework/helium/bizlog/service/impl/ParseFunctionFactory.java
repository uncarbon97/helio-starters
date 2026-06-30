package cc.uncarbon.framework.helium.bizlog.service.impl;

import cc.uncarbon.framework.helium.bizlog.service.IParseFunction;
import cn.hutool.core.text.CharSequenceUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 自定义解析函数工厂
 * <p>
 * 启动时收集所有 {@link IParseFunction} Bean，按 {@link IParseFunction#functionName()} 建表，
 * 提供按名查找与前置判定能力。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
public class ParseFunctionFactory {

    private Map<String, IParseFunction> allFunctionMap;

    /**
     * 收集自定义函数并按函数名建表（函数名为空者跳过）。
     *
     * @param parseFunctions 容器中的自定义函数集合
     */
    public ParseFunctionFactory(List<IParseFunction> parseFunctions) {
        if (CollectionUtils.isEmpty(parseFunctions)) {
            return;
        }
        allFunctionMap = new HashMap<>();
        for (IParseFunction parseFunction : parseFunctions) {
            if (StringUtils.hasText(parseFunction.functionName())) {
                allFunctionMap.put(parseFunction.functionName(), parseFunction);
            }
        }
    }

    /**
     * 按函数名取函数。
     *
     * @param functionName 函数名
     * @return 对应函数，不存在返回 {@code null}
     */
    public IParseFunction getFunction(String functionName) {
        return allFunctionMap.get(functionName);
    }

    /**
     * 判断指定函数是否前置执行。
     *
     * @param functionName 函数名
     * @return 函数存在且声明前置执行返回 {@code true}
     */
    public boolean isBeforeFunction(String functionName) {
        return allFunctionMap.get(functionName) != null && allFunctionMap.get(functionName).executeBefore();
    }
}
