package cc.uncarbon.framework.knife4j.customizer;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.core.MethodParameter;

import java.util.Objects;

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
public class HelioBaseEnumCustomizer implements PropertyCustomizer, ParameterCustomizer {

    /**
     * 主要用于 @Schema 注解标记的字段
     */
    @Override
    public Schema<?> customize(Schema property, AnnotatedType propertyType) {
        if (Objects.nonNull(property) && Objects.nonNull(propertyType)) {
            try {
                SimpleType simpleType = (SimpleType) propertyType.getType();
                if (HelioBaseEnum.class.isAssignableFrom(simpleType.getRawClass())) {
                    property.setDescription(determineDescription(simpleType.getRawClass(), property.getDescription()));
                }
            } catch (Exception e) {
                // fail to customize, ignored
            }
        }
        return property;
    }

    /**
     *
     * 主要用于 @Parameter 注解标记的字段，以及路径参数、Query
     */
    @Override
    public Parameter customize(Parameter parameterModel, MethodParameter methodParameter) {
        if (Objects.nonNull(parameterModel) && Objects.nonNull(methodParameter)
                && HelioBaseEnum.class.isAssignableFrom(methodParameter.getParameterType())) {
            parameterModel.setDescription(determineDescription(methodParameter.getParameterType(), parameterModel.getDescription()));
        }
        return parameterModel;
    }

    /**
     * 确定输出的描述文本
     */
    protected String determineDescription(Class<?> helioBaseEnum, String originalDescription) {
        // 获取枚举类中的所有枚举项
        HelioBaseEnum<?>[] enumItems = (HelioBaseEnum<?>[]) helioBaseEnum.getEnumConstants();

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
