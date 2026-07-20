package cc.uncarbon.framework.helium.base.stream;

import lombok.experimental.UtilityClass;

import java.util.function.BinaryOperator;

/**
 * Stream 流函数
 *
 * @author Uncarbon
 */
@UtilityClass
public final class StreamFunction {

    /**
     * 用于 stream-collect-toMap 遇到相同 key 时，保留现有 value
     * 用法：stream().collect(Collectors.toMap(XXX, YYY, StreamFunction.keepExisting()))
     */
    public static <T> BinaryOperator<T> keepExisting() {
        return (existing, replacement) -> existing;
    }

    /**
     * 用于 stream-collect-toMap 遇到相同 key 时，用新 value 替代旧 value
     * 用法：stream().collect(Collectors.toMap(XXX, YYY, StreamFunction.replaceExisting()))
     */
    public static <T> BinaryOperator<T> replaceExisting() {
        return (existing, replacement) -> replacement;
    }

}
