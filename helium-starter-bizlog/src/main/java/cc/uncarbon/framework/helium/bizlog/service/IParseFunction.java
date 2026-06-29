package cc.uncarbon.framework.helium.bizlog.service;

/**
 * 自定义解析函数
 * <p>
 * 实现该接口并注册为 Spring Bean，即可在 {@code @LogRecord} 模板中以 {@code {函数名{SpEL}}} 形式调用。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public interface IParseFunction {

    /**
     * 是否在业务方法执行前调用（默认否）。
     *
     * @return 前置执行返回 {@code true}
     */
    default boolean executeBefore() {
        return false;
    }

    /**
     * 函数名，对应模板中的 {@code {函数名{...}}}。
     *
     * @return 函数名
     */
    String functionName();

    /**
     * 执行函数逻辑。
     *
     * @param value 函数入参
     * @return 渲染后的文案
     * @since 1.1.0 参数从 String 修改为 Object 类型，可处理更多场景，可通过 SpEL 表达式传递对象；
     * 老版本需修改自定义函数声明，实现中将用到 value 的地方改为 value.toString 即可兼容
     */
    String apply(Object value);
}
