package cc.uncarbon.framework.helio.jackson.module;

import cc.uncarbon.framework.helio.base.enums.BaseEnum;
import cn.hutool.core.text.CharSequenceUtil;
import tools.jackson.core.*;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * 序列化规则模块：BaseEnum及其子类，如果是普通对象字段，另外输出 Label 字段
 *
 * @author Uncarbon
 */
public class BaseEnumFormatModule extends SimpleModule {

    public BaseEnumFormatModule() {
        super(BaseEnumFormatModule.class.getSimpleName(), Version.unknownVersion());
        this.addSerializer(BaseEnum.class, new BaseEnumSerializer(BaseEnum.class));
        this.addDeserializer(BaseEnum.class, new BaseEnumDeserializer<>(BaseEnum.class));
    }

    @SuppressWarnings("rawtypes")
    public static class BaseEnumSerializer extends StdSerializer<BaseEnum> {
        protected BaseEnumSerializer(Class<?> t) {
            super(t);
        }

        @Override
        public void serialize(BaseEnum value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
            gen.writePOJO(value);
            TokenStreamContext writeContext = gen.streamWriteContext();
            // 如果是普通对象字段，另外输出 Label 字段
            if (!writeContext.inArray()) {
                gen.writeStringProperty(writeContext.currentName() + "Label", value.getLabel());
            }
        }
    }

    private static final class BaseEnumDeserializers extends Deserializers.Base {

        @Override
        public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
            return false;
        }

        @Override
        public ValueDeserializer<?> findEnumDeserializer(JavaType type, DeserializationConfig config,
                                                         BeanDescription.Supplier beanDesc) {
            if (BaseEnum.class.isAssignableFrom(type.getRawClass())) {
                return new BaseEnumDeserializer(type.getRawClass());
            }
            return null;
        }
    }

    private static final class BaseEnumDeserializer<E extends BaseEnum<?>> extends StdScalarDeserializer<E> {

        private final Class<E> enumType;

        @SuppressWarnings("unchecked")
        private BaseEnumDeserializer(Class<?> clazz) {
            super(clazz);
            this.enumType = (Class<E>) clazz;
        }

        @Override
        public E deserialize(JsonParser parser, DeserializationContext context) {
            if (parser.currentToken().isNumeric()) {
                return BaseEnum.of(this.enumType, parser.getIntValue())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Cannot convert numeric value to enum " + this.enumType.getSimpleName()));
            }
            if (CharSequenceUtil.isNotBlank(parser.getString())) {
                return BaseEnum.of(this.enumType, parser.getString())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Cannot convert string value to enum " + this.enumType.getSimpleName()));
            }
            throw new IllegalArgumentException(
                    "Cannot convert value to enum " + this.enumType.getSimpleName());
        }
    }
}
