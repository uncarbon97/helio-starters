package cc.uncarbon.framework.helium.i18n.autoconfigure;

import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.message.YamlMessageSource;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cc.uncarbon.framework.helium.i18n.resolver.CompositeLangResolver;
import cc.uncarbon.framework.helium.i18n.resolver.CompositeTimezoneResolver;
import cc.uncarbon.framework.helium.i18n.resolver.LangResolver;
import cc.uncarbon.framework.helium.i18n.resolver.TimezoneResolver;
import cc.uncarbon.framework.helium.i18n.resolver.lang.DefaultCompositeLangResolver;
import cc.uncarbon.framework.helium.i18n.resolver.lang.HeaderLangResolver;
import cc.uncarbon.framework.helium.i18n.resolver.lang.QueryParamLangResolver;
import cc.uncarbon.framework.helium.i18n.resolver.timezone.DefaultCompositeTimezoneResolver;
import cc.uncarbon.framework.helium.i18n.resolver.timezone.HeaderTimezoneResolver;
import cc.uncarbon.framework.helium.i18n.resolver.timezone.QueryParamTimezoneResolver;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Helium 集成国际化自动装配类
 *
 * @author Uncarbon
 */
@Conditional(value = HeliumI18nAutoConfiguration.OnI18nEnabled.class)
@EnableConfigurationProperties(HeliumI18nProperties.class)
@AutoConfiguration
public class HeliumI18nAutoConfiguration {

    // -- 多语言

    @Bean(name = HeliumI18nConstant.MESSAGE_SOURCE_SPRING_BEAN_NAME)
    public MessageSource messageSource(HeliumI18nProperties props) {
        return new YamlMessageSource(props, StandardCharsets.UTF_8);
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryParamLangResolver queryParamLangResolver(HeliumI18nProperties props) {
        return new QueryParamLangResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public HeaderLangResolver headerLangResolver(HeliumI18nProperties props) {
        return new HeaderLangResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public CompositeLangResolver compositeLangResolver(HeliumI18nProperties props, List<LangResolver> resolvers) {
        return new DefaultCompositeLangResolver(props, resolvers);
    }

    // -- 多时区

    @Bean
    @ConditionalOnMissingBean
    public QueryParamTimezoneResolver queryParamTimezoneResolver(HeliumI18nProperties props) {
        return new QueryParamTimezoneResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public HeaderTimezoneResolver headerTimezoneResolver(HeliumI18nProperties props) {
        return new HeaderTimezoneResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public CompositeTimezoneResolver compositeTimezoneResolver(HeliumI18nProperties props, List<TimezoneResolver> resolvers) {
        return new DefaultCompositeTimezoneResolver(props, resolvers);
    }

    protected static class OnI18nEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = Objects.requireNonNull(context.getBeanFactory()).getBean(HeliumI18nProperties.class);
            return Boolean.TRUE.equals(props.getEnabled());
        }
    }
}
