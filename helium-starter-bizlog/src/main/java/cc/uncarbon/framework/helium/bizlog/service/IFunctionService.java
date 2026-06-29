package cc.uncarbon.framework.helium.bizlog.service;

/**
 * 自定义函数调用入口
 * <p>
 * 根据函数名查找对应的 {@code IParseFunction} 并执行，是模板中 {@code {函数名{...}}} 占位符的执行中枢。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public interface IFunctionService {

    /**
     * 按函数名执行自定义函数。
     *
     * @param functionName 函数名
     * @param value        SpEL 求值后的入参
     * @return 函数返回的文案
     */
    String apply(String functionName, Object value);

    /**
     * 判断指定函数是否需要在业务方法执行前调用。
     *
     * @param functionName 函数名
     * @return 前置执行返回 {@code true}
     */
    boolean beforeFunction(String functionName);
}
