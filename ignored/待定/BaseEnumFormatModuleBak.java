package cc.uncarbon.framework.helio.jackson.module;

import cc.uncarbon.framework.helio.base.enums.BaseEnum;
import cn.hutool.core.text.CharSequenceUtil;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.TokenStreamContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * 继承了BaseEnum接口的枚举值，将会统一按照以下格式序列化
 * {
 * "value": "foo",
 * "valueLabel": "bar"
 * }
 *
 * @author hanfeng
 * @author Uncarbon
 **/
public class BaseEnumFormatModuleBak extends SimpleModule {
    public BaseEnumFormatModuleBak() {
        super(BaseEnumFormatModuleBak.class.getSimpleName(), Version.unknownVersion());
        this.setDeserializers(new CustomDeserializers());
        this.addSerializer(new EnumSerializer());
    }

    private static final class CustomDeserializers extends SimpleDeserializers {
        private CustomDeserializers() {
        }

//        @Override
//        public ValueDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config,
//                                                         BeanDescription.Supplier beanDescRef) throws DatabindException {
//            return BaseEnum.class.isAssignableFrom(type)
//                    ? new EnumDeserializer(type)
//                    : super.findEnumDeserializer(type, config, beanDescRef);
//        }

        private static final class EnumDeserializer<E extends BaseEnum<?>> extends StdScalarDeserializer<E> {
            private final Class<E> enumType;

            private EnumDeserializer(Class<E> clazz) {
                super(clazz);
                this.enumType = clazz;
            }

            @Override
            public E deserialize(JsonParser parser, DeserializationContext context) {
                if (parser.currentToken().isNumeric()) {
                    return BaseEnum.of(this.enumType, parser.getIntValue())
                            .orElseThrow(() -> new IllegalArgumentException("Unable to parse input value"));
                } else if (CharSequenceUtil.isNotBlank(parser.getString())) {
                    return BaseEnum.of(this.enumType, parser.getString())
                            .orElseThrow(() -> new IllegalArgumentException("Unable to parse input value"));
                } else {
                    throw new IllegalArgumentException("Unable to parse input value 'cause wrong type");
                }
            }
        }
    }

    private static final class EnumSerializer extends StdSerializer<BaseEnum> {

        private EnumSerializer() {
            super(BaseEnum.class);
        }

        @Override
        public void serialize(BaseEnum data, JsonGenerator jsonGenerator, SerializationContext provider)
                throws IOException {
            jsonGenerator.writePOJO(data.getValue());
            TokenStreamContext outputContext = jsonGenerator.getOutputContext();
            if (!outputContext.inArray()) {
                jsonGenerator.writeFieldName(outputContext.currentName() + "Label");
                jsonGenerator.writeString(data.getLabel());
            }
        }
    }
}
