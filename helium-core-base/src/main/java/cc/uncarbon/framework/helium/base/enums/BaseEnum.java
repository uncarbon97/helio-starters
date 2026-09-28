package cc.uncarbon.framework.helium.base.enums;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Predicate;

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

    /**
     * 根据枚举的{@link BaseEnum#getLabel()} 来查找.
     *
     * @see #find(Class, Predicate)
     */
    static <T extends Enum<?> & BaseEnum<?>> Optional<T> findByLabel(Class<T> type, String text) {
        return find(type, e -> text != null && e.getLabel() != null && e.getLabel().equalsIgnoreCase(text));
    }

    /**
     * 从指定的枚举中查找想要的枚举,并返回一个{@link Optional},如果未找到,则返回一个{@link Optional#empty()}
     *
     * @param type      实现了{@link BaseEnum}的枚举
     * @param predicate 判断逻辑
     * @param <T>       枚举类型
     * @return 查找到的结果
     */
    static <T extends Enum<?> & BaseEnum<?>> Optional<T> find(Class<T> type, Predicate<T> predicate) {
        if (type.isEnum()) {
            for (T each : type.getEnumConstants()) {
                if (predicate.test(each)) {
                    return Optional.of(each);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 根据枚举的{@link BaseEnum#getValue()}来查找.
     *
     * @see #find(Class, Predicate)
     */
    static <T extends Enum<?> & BaseEnum<?>> Optional<T> findByValue(Class<T> type, Object value) {
        return find(type,
                e -> e.getValue() == value
                        || e.getValue().equals(value)
                        || String.valueOf(e.getValue()).equalsIgnoreCase(String.valueOf(value))
        );
    }

    /**
     * 对比是否和value相等,对比地址,值,value转为string忽略大小写对比,text忽略大小写对比
     *
     * @param v value
     * @return 是否相等
     */
    default boolean eq(Object v) {
        if (v == null) {
            return false;
        }
        if (v instanceof Object[] values) {
            // 数组入参：任一元素匹配即视为相等
            for (Object value : values) {
                if (eq(value)) {
                    return true;
                }
            }
            return false;
        }
        return this == v
                || getValue() == v
                || getValue().equals(v)
                || String.valueOf(getValue()).equalsIgnoreCase(String.valueOf(v))
                || getLabel().equalsIgnoreCase(String.valueOf(v)
        );
    }

    /**
     * 根据枚举的{@link BaseEnum#getValue()},{@link BaseEnum#getLabel()} ()}来查找.
     *
     * @see #find(Class, Predicate)
     */
    static <T extends Enum<?> & BaseEnum<?>> Optional<T> find(Class<T> type, Object target) {
        return find(type, v -> v.eq(target));
    }

    static <E extends BaseEnum<?>> Optional<E> of(Class<E> type, Object value) {
        if (type.isEnum()) {
            for (E enumConstant : type.getEnumConstants()) {
                Predicate<E> predicate =
                        e -> e.getValue() == value
                                || e.getValue().equals(value)
                                || String.valueOf(e.getValue()).equalsIgnoreCase(String.valueOf(value));
                if (predicate.test(enumConstant)) {
                    return Optional.of(enumConstant);
                }
            }
        }
        return Optional.empty();
    }
}
