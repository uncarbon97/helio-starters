package cc.uncarbon.framework.core.enums;

import cc.uncarbon.framework.core.exception.BusinessException;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 基础枚举类
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
     * @see this#find(Class, Predicate)
     */
    static <T extends Enum<?> & HelioBaseEnum<?>> Optional<T> findByValue(Class<T> type, Object value) {
        return find(type, e -> e.getValue() == value || e.getValue().equals(value) || String.valueOf(e.getValue()).equalsIgnoreCase(String.valueOf(value)));
    }

    /**
     * 根据枚举的{@link HelioBaseEnum#getLabel()} 来查找.
     *
     * @see this#find(Class, Predicate)
     */
    static <T extends Enum<?> & HelioBaseEnum<?>> Optional<T> findByLabel(Class<T> type, String text) {
        return find(type, e -> e.getLabel().equalsIgnoreCase(text));
    }

    /**
     * 根据枚举的{@link HelioBaseEnum#getValue()},{@link HelioBaseEnum#getLabel()} ()}来查找.
     *
     * @see this#find(Class, Predicate)
     */
    static <T extends Enum<?> & HelioBaseEnum<?>> Optional<T> find(Class<T> type, Object target) {
        return find(type, v -> v.eq(target));
    }

    static <E extends HelioBaseEnum<?>> Optional<E> of(Class<E> type, Object value) {
        if (type.isEnum()) {
            for (E enumConstant : type.getEnumConstants()) {
                Predicate<E> predicate = e -> e.getValue() == value || e.getValue().equals(value) || String.valueOf(e.getValue()).equalsIgnoreCase(String.valueOf(value));
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
    @SuppressWarnings("all")
    default boolean eq(Object v) {
        if (v == null) {
            return false;
        }
        if (v instanceof Object[]) {
            v = Arrays.asList(v);
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
     *
     * @return int value
     */
    default int convertValue2Int() {
        if (this.getValue() == null) {
            throw new IllegalArgumentException("枚举类的 value 不能为 null !");
        }

        if (this.getValue() instanceof Integer) {
            return (Integer) this.getValue();
        }

        if (this.getValue() instanceof Long) {
            return ((Long) this.getValue()).intValue();
        }

        if (this.getValue() instanceof Number) {
            return ((Number) this.getValue()).intValue();
        }

        if (this.getValue() instanceof String) {
            return NumberUtil.toBigDecimal((String) this.getValue()).intValue();
        }

        throw new IllegalArgumentException("枚举类的 value 不能自动转换为 int !");
    }

    /**
     * 断言不为null
     * 注意：枚举选项的 value 建议为整数类型
     *
     * @param object 需要判断的对象
     */
    default void assertNotNull(Object object) {
        if (ObjectUtil.isNotNull(object)) {
            return;
        }

        throw new BusinessException(this.convertValue2Int(), this.getLabel());
    }

    /**
     * 断言不为空文本
     * 注意：枚举选项的 value 建议为整数类型
     *
     * @param str 需要判断的对象
     */
    default void assertNotBlank(CharSequence str) {
        if (StrUtil.isNotBlank(str)) {
            return;
        }

        throw new BusinessException(this.convertValue2Int(), this.getLabel());
    }

    /**
     * 断言不为空列表
     * 注意：枚举选项的 value 建议为整数类型
     *
     * @param list 需要判断的对象
     */
    default void assertNotEmpty(Iterable<?> list) {
        if (IterUtil.isNotEmpty(list)) {
            return;
        }

        throw new BusinessException(this.convertValue2Int(), this.getLabel());
    }

}
