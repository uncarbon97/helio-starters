package cc.uncarbon.framework.helium.bizlog.service.impl;


import cc.uncarbon.framework.helium.bizlog.service.IFunctionService;
import cc.uncarbon.framework.helium.bizlog.service.IParseFunction;
import lombok.RequiredArgsConstructor;

/**
 * 自定义函数服务默认实现
 * <p>
 * 委托 {@link ParseFunctionFactory} 按函数名查找并执行；
 * 未找到对应函数时，直接将入参转为字符串返回。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class DefaultFunctionServiceImpl implements IFunctionService {

    private final ParseFunctionFactory parseFunctionFactory;

    @Override
    public String apply(String functionName, Object value) {
        IParseFunction function = parseFunctionFactory.getFunction(functionName);
        if (function == null) {
            return value.toString();
        }
        return function.apply(value);
    }

    @Override
    public boolean beforeFunction(String functionName) {
        return parseFunctionFactory.isBeforeFunction(functionName);
    }
}
