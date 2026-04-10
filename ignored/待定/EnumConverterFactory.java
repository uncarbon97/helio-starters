package cc.uncarbon.framework.helio.jackson;

import cc.uncarbon.framework.helio.base.enums.BaseEnum;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 枚举转换
 *
 * @author hanfeng
 */
public class EnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    @SuppressWarnings("rawtypes")
    private final Map<Class, Converter> converterCache = new WeakHashMap<>();

    @SuppressWarnings("rawtypes")
    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(@NonNull Class<T> targetType) {
        return converterCache.computeIfAbsent(targetType,
                k -> converterCache.put(k, new EnumConverter(k))
        );
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
