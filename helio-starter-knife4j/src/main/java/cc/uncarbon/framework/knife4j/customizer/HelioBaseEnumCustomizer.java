package cc.uncarbon.framework.knife4j.customizer;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * HelioBaseEnum子枚举增强
 * <p>
 * 示例代码：<br />
 * {@code @Schema(description} = "性别")<br />
 * private GenderEnum gender;
 * <p>
 * 输出文档描述：
 * 性别(0=未知 1=男 2=女)
 *
 * @author Uncarbon
 */
@Component
public class HelioBaseEnumCustomizer implements PropertyCustomizer, ParameterCustomizer {

    protected static final String SCHEMA_TYPE_INTEGER = "integer";
    protected static final String SCHEMA_FORMAT_INT32 = "int32";
    protected static final String SCHEMA_FORMAT_INT64 = "int64";

    /**
     * 主要用于 @Schema 注解标记的字段
     */
    @Override
    public Schema<?> customize(Schema schema, AnnotatedType propertyType) {
        if (Objects.nonNull(schema) && Objects.nonNull(propertyType)) {
            try {
                Type propertyTypeType = propertyType.getType();
                if (propertyTypeType instanceof SimpleType simpleType) {
                    // 非数组形式
                    Class<?> rawClass = simpleType.getRawClass();
                    if (HelioBaseEnum.class.isAssignableFrom(rawClass)) {
                        HelioBaseEnum<?>[] enumItems = (HelioBaseEnum<?>[]) rawClass.getEnumConstants();
                        updateSchema(schema, enumItems, false);
                    }
                } else if (propertyTypeType instanceof CollectionType collectionType) {
                    // 数组形式
                    Class<?> rawClass = Optional.of(collectionType)
                            .map(TypeBase::getBindings)
                            .map(TypeBindings::getTypeParameters)
                            .map(CollUtil::getFirst)
                            .map(JavaType::getRawClass)
                            .orElse(null);
                    if (rawClass != null && HelioBaseEnum.class.isAssignableFrom(rawClass)) {
                        HelioBaseEnum<?>[] enumItems = (HelioBaseEnum<?>[]) rawClass.getEnumConstants();
                        updateSchema(schema, enumItems, true);
                    }
                }
            } catch (Exception ignore) {}
        }
        return schema;
    }

    /**
     * 主要用于 @Parameter 注解标记的字段，以及路径参数、Query
     */
    @Override
    public Parameter customize(Parameter parameterModel, MethodParameter methodParameter) {
        if (Objects.nonNull(parameterModel) && Objects.nonNull(methodParameter)
                && HelioBaseEnum.class.isAssignableFrom(methodParameter.getParameterType())) {
            HelioBaseEnum<?>[] enumItems = (HelioBaseEnum<?>[]) methodParameter.getParameterType().getEnumConstants();
            // 只变更描述
            parameterModel.setDescription(determineDescription(enumItems, parameterModel.getDescription()));
        }
        return parameterModel;
    }

    /**
     * @param isArray 枚举项是否按数组形式展现
     */
    @SuppressWarnings(value = "unchecked")
    protected void updateSchema(Schema schema, HelioBaseEnum<?>[] enumItems, boolean isArray) {
        if (ArrayUtil.isEmpty(enumItems)) {
            return;
        }
        Object firstItemValue = enumItems[0].getValue();
        if (!isArray) {
            // 非数组形式
            markSchemaTypeAsInteger(firstItemValue, schema);
        } else {
            // 数组形式
            markSchemaTypeAsInteger(firstItemValue, schema.getItems());
        }
        // 外显时统一转换为字符串
        schema.setEnum(Arrays.stream(enumItems).map(HelioBaseEnum::getValue).map(StrUtil::toStringOrNull).toList());
        schema.setDescription(determineDescription(enumItems, schema.getDescription()));
    }

    /**
     * 为整数类型特别标注，免得都外显成string类型，导致文档显示有误
     */
    protected void markSchemaTypeAsInteger(Object firstItemValue, Schema schema) {
        if (firstItemValue instanceof Integer) {
            schema.setType(SCHEMA_TYPE_INTEGER);
            schema.setFormat(SCHEMA_FORMAT_INT32);
            schema.setExample(firstItemValue);
        } else if (firstItemValue instanceof Long) {
            schema.setType(SCHEMA_TYPE_INTEGER);
            schema.setFormat(SCHEMA_FORMAT_INT64);
            schema.setExample(firstItemValue);
        }
    }

    /**
     * 确定输出的描述文本
     */
    protected String determineDescription(HelioBaseEnum<?>[] enumItems, String originalDescription) {
        // 拼接描述字符串
        StringBuilder newDescription = new StringBuilder(128)
                .append(originalDescription)
                .append('(');

        for (int i = 0; i < enumItems.length; i++) {
            newDescription
                    .append(enumItems[i].getValue())
                    .append('=')
                    .append(enumItems[i].getLabel());

            if (i < enumItems.length - 1) {
                // 不是最后一项，增加分割符
                newDescription.append(' ');
            }
        }
        newDescription.append(")<br />");
        return newDescription.toString();
    }
}
