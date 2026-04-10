package cc.uncarbon.framework.helio.base.util;

import java.util.function.BinaryOperator;

/**
 * Stream 流工具
 *
 * @author Uncarbon
 */
public final class StreamFunction {

    private StreamFunction() {
    }

    /**
     * 用于 stream-collect-toMap 遇到相同 key 时，保留原有 value 不变化
     * 用法：stream().collect(Collectors.toMap(XXX, YYY, StreamFunction.keepExisting()))
     */
    public static <T> BinaryOperator<T> keepExisting() {
        return (existing, replacement) -> existing;
    }

}
