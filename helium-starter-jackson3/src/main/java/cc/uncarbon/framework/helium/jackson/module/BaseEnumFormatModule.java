package cc.uncarbon.framework.helium.jackson.module;

import cc.uncarbon.framework.helium.base.enums.BaseEnum;
import cc.uncarbon.framework.helium.jackson.props.BaseEnumConfig;
import cn.hutool.core.text.CharSequenceUtil;
import tools.jackson.core.*;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * 序列化规则模块：BaseEnum及其子类，如果是普通对象字段，另外输出 Label 字段
 *
 * @author Uncarbon
 */
public class BaseEnumFormatModule extends SimpleModule {

    public BaseEnumFormatModule(BaseEnumConfig baseEnumConfig) {
        super(BaseEnumFormatModule.class.getSimpleName(), Version.unknownVersion());
        this.addSerializer(BaseEnum.class, new BaseEnumSerializer(BaseEnum.class, baseEnumConfig));
        this.setDeserializers(new BaseEnumDeserializers());
    }

    @SuppressWarnings("rawtypes")
    public static class BaseEnumSerializer extends StdSerializer<BaseEnum> {

        private final BaseEnumConfig baseEnumConfig;
        protected BaseEnumSerializer(Class<?> t, BaseEnumConfig baseEnumConfig) {
            super(t);
            this.baseEnumConfig = baseEnumConfig;
        }

        @Override
        public void serialize(BaseEnum value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
            gen.writePOJO(value.getValue());
            if (baseEnumConfig.showLabel()) {
                TokenStreamContext writeContext = gen.streamWriteContext();
                // 如果是普通对象字段，另外输出 xxxLabel 字段
                if (!writeContext.inArray()) {
                    gen.writeStringProperty(writeContext.currentName() + "Label", value.getLabel());
                }
            }
        }
    }

    private static final class BaseEnumDeserializers extends SimpleDeserializers {

        @Override
        public ValueDeserializer<?> findEnumDeserializer(JavaType enumType, DeserializationConfig config,
                                                         BeanDescription.Supplier beanDescRef) {
            if (BaseEnum.class.isAssignableFrom(enumType.getRawClass())) {
                return new BaseEnumDeserializer(enumType.getRawClass());
            }
            return super.findEnumDeserializer(enumType, config, beanDescRef);
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
            String enumSimpleName = this.enumType.getSimpleName();
            if (parser.currentToken().isNumeric()) {
                return BaseEnum.of(this.enumType, parser.getIntValue())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Cannot convert numeric value to enum " + enumSimpleName));
            }
            if (CharSequenceUtil.isNotBlank(parser.getString())) {
                return BaseEnum.of(this.enumType, parser.getString())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Cannot convert string value to enum " + enumSimpleName));
            }
            throw new IllegalArgumentException(
                    "Cannot convert value to enum " + enumSimpleName);
        }
    }
}
