package cc.uncarbon.framework.helium.base.enums;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * 标记一个类属于基础枚举
 *
 * @param <T> 值类型
 * @author hanfeng
 * @author Uncarbon
 */
public interface BaseEnum<T extends Serializable> extends Serializable {

    /**
     * @return 返回枚举项的值，通常由字母或者数字组成，并且在同一个枚举中值唯一；对应数据库中的值通常也为此值
     */
    T getValue();

    /**
     * @return 返回枚举项的描述文本
     */
    String getLabel();

    static <E extends BaseEnum<?>> Optional<E> of(Class<E> type, Object value) {
        if (type.isEnum()) {
            for (E enumConstant : type.getEnumConstants()) {
                Object enumValue = enumConstant.getValue();
                boolean matched =
                        Objects.equals(enumValue, value)
                                || (enumValue != null && String.valueOf(enumValue).equalsIgnoreCase(String.valueOf(value)));
                if (matched) {
                    return Optional.of(enumConstant);
                }
            }
        }
        return Optional.empty();
    }
}
