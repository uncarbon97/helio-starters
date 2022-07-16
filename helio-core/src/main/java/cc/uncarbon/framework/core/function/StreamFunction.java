package cc.uncarbon.framework.core.function;

import java.util.function.BinaryOperator;

/**
 * Stream 流函数
 * 其实更像是一个另类的工具方法集合
 *
 * @author Uncarbon
 */
public class StreamFunction {

    /**
     * 用于 stream-collect-toMap 时，忽略已存在的 key & value；不忽略的话，遇到重复项时默认会抛出异常
     *
     * 用法：stream().collect(Collectors.toMap( XXX, XXX, StreamFunction.ignoredThrowingMerger()))
     *
     * @param <T> 键类型
     * @return 键名
     */
    public static <T> BinaryOperator<T> ignoredThrowingMerger() {
        return (u, v) -> u;
    }

}
