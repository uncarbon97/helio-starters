package cc.uncarbon.framework.core.enums;

import cc.uncarbon.framework.core.exception.BusinessException;
import cc.uncarbon.framework.core.exception.HelioFrameworkException;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 基础枚举类
 * @param <T> 枚举类value字段的类型，建议为整数类型
 *
 * @author Zhu JW
 * @author Uncarbon
 **/
public interface HelioBaseEnum<T> extends Serializable {

    /**
     * 从指定的枚举类中查找想要的枚举,并返回一个{@link Optional},如果未找到,则返回一个{@link Optional#empty()}
     *
     * @param type      实现了{@link HelioBaseEnum}的枚举类
     * @param predicate 判断逻辑
     * @param <T>       枚举类型
     * @return 查找到的结果
     */
    static <T extends Enum<?> & HelioBaseEnum<?>> Optional<T> find(Class<T> type, Predicate<T> predicate) {
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
     * 根据枚举的{@link HelioBaseEnum#getValue()}来查找.
     *
     * @see #find(Class, Predicate)
     */
    static <T extends Enum<?> & HelioBaseEnum<?>> Optional<T> findByValue(Class<T> type, Object value) {
        return find(type,
                e -> e.getValue() == value
                        || e.getValue().equals(value)
                        || String.valueOf(e.getValue()).equalsIgnoreCase(String.valueOf(value))
        );
    }

    /**
     * 根据枚举的{@link HelioBaseEnum#getLabel()} 来查找.
     *
     * @see #find(Class, Predicate)
     */
    static <T extends Enum<?> & HelioBaseEnum<?>> Optional<T> findByLabel(Class<T> type, String text) {
        return find(type, e -> e.getLabel().equalsIgnoreCase(text));
    }

    /**
     * 根据枚举的{@link HelioBaseEnum#getValue()},{@link HelioBaseEnum#getLabel()} ()}来查找.
     *
     * @see #find(Class, Predicate)
     */
    static <T extends Enum<?> & HelioBaseEnum<?>> Optional<T> find(Class<T> type, Object target) {
        return find(type, v -> v.eq(target));
    }

    static <E extends HelioBaseEnum<?>> Optional<E> of(Class<E> type, Object value) {
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

    /**
     * 枚举选项的值,通常由字母或者数字组成,并且在同一个枚举中值唯一;对应数据库中的值通常也为此值
     *
     * @return 枚举的值
     */
    T getValue();

    /**
     * 枚举选项的文本，通常为中文
     *
     * @return 枚举的文本
     */
    String getLabel();

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
        if (v instanceof Object[]) {
            v = Collections.singletonList(v);
        }
        return this == v
                || getValue() == v
                || getValue().equals(v)
                || String.valueOf(getValue()).equalsIgnoreCase(String.valueOf(v))
                || getLabel().equalsIgnoreCase(String.valueOf(v)
        );
    }

    /**
     * 将不定类型的 value 统一转换为 int
     * 按常见的数据类型依次判断，提高命中率
     * @deprecated since 1.10.1, replaced with getValueAsInt()
     *
     * @return int value
     */
    @Deprecated
    default int convertValue2Int() {
        return getValueAsInt();
    }

    /**
     * 将不定类型的 value 统一转换为 int
     * 按常见的数据类型依次判断，提高命中率
     *
     * @return int value
     */
    default int getValueAsInt() {
        T value = getValue();
        if (value == null) {
            throw new IllegalArgumentException("enum's value CANNOT be null");
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return NumberUtil.toBigDecimal((String) value).intValue();
        }
        throw new HelioFrameworkException("enum's value CANNOT convert to int");
    }

    default String formatLabel(Object... templateParams) {
        if (ArrayUtil.isEmpty(templateParams)) {
            return getLabel();
        }

        return CharSequenceUtil.format(getLabel(), templateParams);
    }

    /**
     * 断言不为null
     *
     * @param object 需要判断的对象
     * @param templateParams label 中如果有占位符的话，向里面填充的模板参数
     */
    default void assertNotNull(Object object, Object... templateParams) {
        assertTrue(Objects.nonNull(object), templateParams);
    }

    /**
     * 断言不为空文本
     *
     * @param cs 需要判断的文本
     * @param templateParams label 中如果有占位符的话，向里面填充的模板参数
     */
    default void assertNotBlank(CharSequence cs, Object... templateParams) {
        assertTrue(CharSequenceUtil.isNotBlank(cs), templateParams);
    }

    /**
     * 断言不为空集合
     *
     * @param iterable 需要判断的集合
     * @param templateParams label 中如果有占位符的话，向里面填充的模板参数
     */
    default void assertNotEmpty(Iterable<?> iterable, Object... templateParams) {
        assertTrue(IterUtil.isNotEmpty(iterable), templateParams);
    }

    /**
     * 断言为真
     *
     * @param expression 需要判断的表达式
     * @param templateParams label 中如果有占位符的话，向里面填充的模板参数
     */
    default void assertTrue(boolean expression, Object... templateParams) {
        if (!expression) {
            throw new BusinessException(this, templateParams);
        }
    }

    /**
     * 断言为假
     *
     * @param expression 需要判断的表达式
     * @param templateParams label 中如果有占位符的话，向里面填充的模板参数
     */
    default void assertFalse(boolean expression, Object... templateParams) {
        if (expression) {
            throw new BusinessException(this, templateParams);
        }
    }

}
