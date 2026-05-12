package cc.uncarbon.framework.helio.openapi.autoconfigure;

import cc.uncarbon.framework.helio.openapi.props.HelioOpenApi3Properties;
import cc.uncarbon.framework.helio.openapi.nextdoc4j.BaseEnumResolver;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

/**
 * Helio 集成 OpenAPI 3 自动配置类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HelioOpenApi3Properties.class})
@AutoConfiguration
public class HelioOpenApi3AutoConfiguration {


    @Conditional(value = OnNextdoc4jEnabled.class)
    @Bean
    public BaseEnumResolver baseEnumResolver() {
        return new BaseEnumResolver();
    }

    private static class OnNextdoc4jEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = Objects.requireNonNull(context.getBeanFactory()).getBean(HelioOpenApi3Properties.class);
            var subProp = props.getNextdoc4j();
            return Boolean.TRUE.equals(subProp.getEnabled());
        }
    }
}
