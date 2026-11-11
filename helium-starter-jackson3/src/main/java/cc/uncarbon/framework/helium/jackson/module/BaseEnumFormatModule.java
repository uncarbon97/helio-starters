package cc.uncarbon.framework.helium.jackson.module;

import cc.uncarbon.framework.helium.base.enums.BaseEnum;
import cc.uncarbon.framework.helium.base.exception.ValueParseException;
import cc.uncarbon.framework.helium.jackson.props.BaseEnumConfig;
import cn.hutool.core.text.CharSequenceUtil;
import tools.jackson.core.*;
import tools.jackson.core.exc.InputCoercionException;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Optional;

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
                /*
                仅普通对象字段附加 xxxLabel 字段；
                根值场景（writeValueAsString(enum)、Redis 直接存枚举等）currentName 为 null，
                在根上下文写属性名会抛 StreamWriteException
                 */
                if (!writeContext.inArray() && writeContext.currentName() != null) {
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
                // JsonParser#getIntValue() 对小数 / 超 int 范围值抛 InputCoercionException（非 ValueParseException），
                // 绕开框架统一的 400 异常处理；这里先判定数值类型与范围，不能安全落入 int 则按业务校验失败处理
                JsonParser.NumberType numberType = parser.getNumberType();
                boolean isIntegral = numberType == JsonParser.NumberType.INT
                        || numberType == JsonParser.NumberType.LONG
                        || numberType == JsonParser.NumberType.BIG_INTEGER;
                if (!isIntegral || !canFitInInt(parser)) {
                    throw new ValueParseException("无法将数值 %s 转换为 %s（须为整数且在 int 范围内）".formatted(
                            parser.getText(), enumSimpleName));
                }
                int numericValue = parser.getIntValue();
                Optional<E> op = BaseEnum.of(this.enumType, numericValue);
                if (op.isPresent()) {
                    return op.get();
                }
                throw new ValueParseException("无法将数值 %s 转换为 %s".formatted(
                        String.valueOf(numericValue), enumSimpleName));
            }
            String strValue = parser.getString();
            if (CharSequenceUtil.isNotBlank(strValue)) {
                Optional<E> op = BaseEnum.of(this.enumType, strValue);
                if (op.isPresent()) {
                    return op.get();
                }
                throw new ValueParseException("无法将文本 %s 转换为 %s".formatted(
                        strValue, enumSimpleName));
            }
            throw new ValueParseException("无法将值转换为 %s".formatted(enumSimpleName));
        }

        /**
         * 数值是否能安全落入 int 范围（不截断、不抛 {@link InputCoercionException}）
         */
        private static boolean canFitInInt(JsonParser parser) {
            try {
                long longValue = parser.getLongValue();
                return longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
