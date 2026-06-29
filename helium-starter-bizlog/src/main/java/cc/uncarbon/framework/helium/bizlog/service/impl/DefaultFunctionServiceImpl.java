package cc.uncarbon.framework.helium.bizlog.service.impl;


import cc.uncarbon.framework.helium.bizlog.service.IFunctionService;
import cc.uncarbon.framework.helium.bizlog.service.IParseFunction;

/**
 * 自定义函数服务默认实现
 * <p>
 * 委托 {@link ParseFunctionFactory} 按函数名查找并执行；
 * 未找到对应函数时，直接将入参转为字符串返回。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
public class DefaultFunctionServiceImpl implements IFunctionService {

    private final ParseFunctionFactory parseFunctionFactory;

    /**
     * 注入函数工厂。
     *
     * @param parseFunctionFactory 函数工厂
     */
    public DefaultFunctionServiceImpl(ParseFunctionFactory parseFunctionFactory) {
        this.parseFunctionFactory = parseFunctionFactory;
    }

    /**
     * 按函数名执行；未注册的函数直接返回入参字符串形式。
     *
     * @param functionName 函数名
     * @param value        入参
     * @return 函数返回的文案
     */
    @Override
    public String apply(String functionName, Object value) {
        IParseFunction function = parseFunctionFactory.getFunction(functionName);
        if (function == null) {
            return value.toString();
        }
        return function.apply(value);
    }

    /**
     * 判断函数是否前置执行。
     *
     * @param functionName 函数名
     * @return 前置执行返回 {@code true}
     */
    @Override
    public boolean beforeFunction(String functionName) {
        return parseFunctionFactory.isBeforeFunction(functionName);
    }
}
