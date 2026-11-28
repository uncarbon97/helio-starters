package cc.uncarbon.framework.helium.bizlog.service.impl;


import cc.uncarbon.framework.helium.bizlog.service.IParseFunction;

/**
 * 兜底的自定义解析函数
 * <p>
 * 函数名为 {@code null}，所有方法均返回空，作为未匹配到具体函数时的默认实现。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
public class DefaultParseFunction implements IParseFunction {

    /**
     * 是否在业务方法执行前调用。
     *
     * @return 默认前置执行
     */
    @Override
    public boolean executeBefore() {
        return true;
    }

    /**
     * 函数名。
     *
     * @return {@code null}，表示兜底
     */
    @Override
    public String functionName() {
        return null;
    }

    /**
     * 函数逻辑。
     *
     * @param value 入参
     * @return 永远返回 {@code null}
     */
    @Override
    public String apply(Object value) {
        return null;
    }
}
