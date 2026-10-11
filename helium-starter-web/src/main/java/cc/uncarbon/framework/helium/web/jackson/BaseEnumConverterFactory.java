package cc.uncarbon.framework.helium.web.jackson;

import cc.uncarbon.framework.helium.base.enums.BaseEnum;
import cc.uncarbon.framework.helium.base.exception.ValueParseException;
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
public class BaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    private final Map<Class, Converter> converterCache = new ConcurrentHashMap<>();


    @Override
    public <T extends BaseEnum> @Nullable Converter<String, T> getConverter(@NonNull Class<T> targetType) {
        // 注意：映射函数必须返回 converter 本身；返回 put() 的旧值 null 会导致首次调用整体返回 null
        return converterCache.computeIfAbsent(targetType, EnumConverter::new);
    }

    protected static class EnumConverter<T extends BaseEnum<T>> implements Converter<Object, T> {

        private final Class<T> enumType;

        public EnumConverter(@NonNull Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(@NonNull Object value) {
            return BaseEnum.of(this.enumType, value)
                    .orElseThrow(() -> new ValueParseException("无法将值转换为 BaseEnum"));
        }
    }
}
