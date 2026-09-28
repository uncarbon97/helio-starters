package cc.uncarbon.framework.helium.web.jackson;

import cc.uncarbon.framework.helium.base.enums.BaseEnum;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举转换
 *
 * @author hanfeng
 */
@SuppressWarnings("rawtypes")
public class EnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    /**
     * 线程安全缓存（键为 Class，生命周期与 ClassLoader 一致，无内存泄漏顾虑）
     */
    private final Map<Class, Converter> converterCache = new ConcurrentHashMap<>();

    @Override
    public <T extends BaseEnum> @Nullable Converter<String, T> getConverter(@NonNull Class<T> targetType) {
        // 注意：映射函数必须返回 converter 本身；返回 put() 的旧值 null 会导致首次调用整体返回 null
        return converterCache.computeIfAbsent(targetType, k -> new EnumConverter(k));
    }

    protected static class EnumConverter<T extends BaseEnum<T>> implements Converter<Object, T> {

        private final Class<T> enumType;

        public EnumConverter(@NonNull Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(@NonNull Object value) {
            return BaseEnum.of(this.enumType, value)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot convert value to BaseEnum"));
        }
    }
}
