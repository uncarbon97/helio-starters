package cc.uncarbon.framework.helium.base.enums;

import cc.uncarbon.framework.helium.base.exception.BusinessException;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.text.CharSequenceUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

/**
 * 标记一个类属于错误码枚举
 *
 * @param <T> 错误码类型
 * @author Uncarbon
 */
public interface ErrorCodeEnum<T extends Serializable> extends Serializable {

    /**
     * @return 返回错误码，通常由字母或者数字组成，并且在同一个枚举中值唯一
     */
    T getErrorCode();

    /**
     * @return 返回错误原因描述，支持使用 {} 作为占位符
     */
    String getErrorDescription();

    /**
     * @return 返回错误原因描述的国际化文本，支持使用 {} 作为占位符
     */
    default String getErrorDescriptionI18n() {
        return getErrorDescription();
    }

    /**
     * 如果对象为null，则抛出 {@link BusinessException}
     *
     * @param object         需要判断的对象
     * @param templateParams 填充枚举中 label 模板的参数
     */
    default void throwIfNull(Object object, Object... templateParams) {
        throwIf(Objects.isNull(object), templateParams);
    }

    /**
     * 如果文本去除所有空格后为空文本，则抛出 {@link BusinessException}
     *
     * @param cs             需要判断的文本
     * @param templateParams 填充枚举中 label 模板的参数
     */
    default void throwIfBlank(CharSequence cs, Object... templateParams) {
        throwIf(CharSequenceUtil.isBlank(cs), templateParams);
    }

    /**
     * 如果文本为空文本，则抛出 {@link BusinessException}
     *
     * @param cs             需要判断的文本
     * @param templateParams 填充枚举中 label 模板的参数
     */
    default void throwIfEmpty(CharSequence cs, Object... templateParams) {
        throwIf(CharSequenceUtil.isEmpty(cs), templateParams);
    }

    /**
     * 如果集合为空，则抛出 {@link BusinessException}
     *
     * @param iterable       需要判断的集合
     * @param templateParams 填充枚举中 label 模板的参数
     */
    default void throwIfEmpty(Iterable<?> iterable, Object... templateParams) {
        throwIf(IterUtil.isEmpty(iterable), templateParams);
    }

    /**
     * 如果集合中存在指定元素，则抛出 {@link BusinessException}
     *
     * @param collection     需要判断的集合
     * @param item           需要寻找的集合元素
     * @param templateParams 填充枚举中 label 模板的参数
     */
    default <E> void throwIfContains(Collection<E> collection, E item, Object... templateParams) {
        throwIf(CollUtil.contains(collection, item), templateParams);
    }

    /**
     * 如果集合中不存在指定元素，则抛出 {@link BusinessException}
     *
     * @param collection     需要判断的集合
     * @param item           需要寻找的集合元素
     * @param templateParams 填充枚举中 label 模板的参数
     */
    default <E> void throwIfNotContains(Collection<E> collection, E item, Object... templateParams) {
        throwIf(!CollUtil.contains(collection, item), templateParams);
    }

    /**
     * 如果为真，则抛出异常
     *
     * @param expression     需要判断的表达式
     * @param templateParams 填充枚举中 label 模板的参数
     */
    default void throwIf(boolean expression, Object... templateParams) throws BusinessException {
        if (expression) {
            throw0(templateParams);
        }
    }

    /**
     * 快速抛出 {@link BusinessException}
     *
     * @param templateParams 填充枚举中 label 模板的参数
     */
    default void throw0(Object... templateParams) throws BusinessException {
        T errorCode = getErrorCode();
        if (errorCode instanceof Integer errorCodeAsInt) {
            throw new BusinessException(errorCodeAsInt, getErrorDescriptionI18n(), templateParams);
        }
        if (errorCode instanceof String errorCodeAsString) {
            throw new BusinessException(errorCodeAsString, getErrorDescriptionI18n(), templateParams);
        }
        throw new IllegalArgumentException("ErrorCode requires Integer or String");
    }

}
