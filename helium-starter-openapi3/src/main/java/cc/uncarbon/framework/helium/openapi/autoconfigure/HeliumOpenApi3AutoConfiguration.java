package cc.uncarbon.framework.helium.openapi.autoconfigure;

import cc.uncarbon.framework.helium.base.condition.HeliumConditions;
import cc.uncarbon.framework.helium.openapi.nextdoc4j.BaseEnumResolver;
import cc.uncarbon.framework.helium.openapi.props.HeliumOpenApi3Properties;
import io.swagger.v3.oas.models.OpenAPI;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Helium 集成 OpenAPI 3 自动装配类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HeliumOpenApi3Properties.class})
@AutoConfiguration
public class HeliumOpenApi3AutoConfiguration {

    /**
     * 如果工程层自定义了 OpenAPI 相关的 Bean，则不创建空 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openAPI() {
        return new OpenAPI();
    }

    @Conditional(value = OnNextdoc4jEnabled.class)
    @Bean
    public BaseEnumResolver baseEnumResolver() {
        return new BaseEnumResolver();
    }

    static class OnNextdoc4jEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = HeliumConditions.bind(context.getEnvironment(), HeliumOpenApi3Properties.class);
            var sub = props.getNextdoc4j();
            return sub != null && Boolean.TRUE.equals(sub.getEnabled());
        }
    }
}
