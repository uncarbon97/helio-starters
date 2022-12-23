package cc.uncarbon.framework.web.jackson;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import cc.uncarbon.framework.core.exception.BusinessException;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 枚举转换
 *
 * @author Zhu JW
 **/
@SuppressWarnings({"rawtypes", "unchecked"})
public class EnumConverterFactory implements ConverterFactory<String, HelioBaseEnum> {
    private final Map<Class, Converter> converterCache = new WeakHashMap<>();

    @Override
    public <T extends HelioBaseEnum> Converter<String, T> getConverter(@NonNull Class<T> targetType) {
        return converterCache.computeIfAbsent(targetType,
                k -> converterCache.put(k, new EnumConverter(k))
        );
    }

    protected static class EnumConverter<T extends HelioBaseEnum<T>> implements Converter<Object, T> {

        private final Class<T> enumType;

        public EnumConverter(@NonNull Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(@NonNull Object value) {
            return HelioBaseEnum.of(this.enumType, value).orElseThrow(() -> new BusinessException("Contains illegal enumeration value"));
        }
    }
}
