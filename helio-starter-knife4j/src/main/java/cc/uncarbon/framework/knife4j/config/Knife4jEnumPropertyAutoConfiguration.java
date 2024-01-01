package cc.uncarbon.framework.knife4j.config;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import springfox.documentation.schema.Annotations;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.swagger.schema.ApiModelProperties;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

/**
 * knife4j 枚举增强自动配置类
 * 非生产环境下，为 knife4j 文档生成枚举类描述
 * <p>
 * 示例代码：<br />
 * {@code @ApiModelProperty(value} = "性别")<br />
 * private GenderEnum gender;
 * <p>
 * 输出文档描述：
 * 性别(0=未知 1=男 2=女)
 *
 * @author Demon-HY
 * @author Uncarbon
 */
@ConditionalOnProperty(prefix = "knife4j", value = "production", havingValue = "false")
@AutoConfiguration
public class Knife4jEnumPropertyAutoConfiguration implements ModelPropertyBuilderPlugin {

    @Override
    public void apply(ModelPropertyContext context) {
        Optional<ApiModelProperty> annotation = Optional.empty();
        Class<?> rawPrimaryType = null;

        Optional<AnnotatedElement> annotatedElement = context.getAnnotatedElement();
        if (annotatedElement.isPresent()) {
            annotation = ApiModelProperties.findApiModePropertyAnnotation(annotatedElement.get());
        }

        Optional<BeanPropertyDefinition> beanPropertyDefinition = context.getBeanPropertyDefinition();
        if (beanPropertyDefinition.isPresent()) {
            annotation = Annotations.findPropertyAnnotation(beanPropertyDefinition.get(), ApiModelProperty.class);
            rawPrimaryType = beanPropertyDefinition.get().getRawPrimaryType();
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
                    .append("(");

            for (int i = 0; i < enumItems.length; i++) {
                newDescription
                        .append(enumItems[i].getValue())
                        .append("=")
                        .append(enumItems[i].getLabel());

                if (i < enumItems.length - 1) {
                    // 不是最后一项，增加分割符
                    newDescription.append(" ");
                }
            }
            newDescription.append(")<br />");

            // 更新文档中的描述
            context.getSpecificationBuilder().description(newDescription.toString());
        }
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}
