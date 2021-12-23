package cc.uncarbon.framework.knife4j.config;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.Annotations;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.swagger.schema.ApiModelProperties;

import java.util.Arrays;
import java.util.Optional;

/**
 * 非生产环境下，为 Knife4j 文档生成枚举类描述
 * 示例代码：
 *     @ApiModelProperty(value = "性别")
 *     private GenderEnum gender;
 *
 * 输出文档描述：
 *     性别(0=未知 1=男 2=女)
 *
 * @author Demon-HY
 * @author Uncarbon
 */
@ConditionalOnProperty(prefix = "knife4j", value = "production", havingValue = "false")
@Component
public class Knife4jEnumPropertyConfig implements ModelPropertyBuilderPlugin {

    @Override
    public void apply(ModelPropertyContext context) {

        Optional<ApiModelProperty> annotation = Optional.empty();
        Class<?> rawPrimaryType = null;

        if (context.getAnnotatedElement().isPresent()) {
            annotation = ApiModelProperties.findApiModePropertyAnnotation(context.getAnnotatedElement().get());
        }

        if (context.getBeanPropertyDefinition().isPresent()) {
            annotation = Annotations.findPropertyAnnotation(
                    context.getBeanPropertyDefinition().get(),
                    ApiModelProperty.class);

            rawPrimaryType = context.getBeanPropertyDefinition().get().getRawPrimaryType();
        }

        // 过滤得到目标类型
        if (annotation.isPresent() && rawPrimaryType != null && HelioBaseEnum.class.isAssignableFrom(rawPrimaryType)) {
            // 获取枚举类中的所有枚举项
            HelioBaseEnum<?>[] enumItems = (HelioBaseEnum<?>[]) rawPrimaryType.getEnumConstants();

            /*
            拼接描述字符串
             */
            StringBuilder newDescription = new StringBuilder(128)
                    .append(annotation.get().value())
                    .append("(")
                    ;
            Arrays.stream(enumItems).forEach(
                    each -> newDescription
                            .append(each.getValue())
                            .append("=")
                            .append(each.getLabel())
            );
            newDescription.append(")");

            // 更新文档中的描述
            context.getSpecificationBuilder().description(newDescription.toString());
        }
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}
