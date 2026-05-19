package cc.uncarbon.framework.helio.i18n.autoconfigure;

import cc.uncarbon.framework.helio.i18n.constant.HelioI18nConstant;
import cc.uncarbon.framework.helio.i18n.message.YamlMessageSource;
import cc.uncarbon.framework.helio.i18n.props.HelioI18nProperties;
import cc.uncarbon.framework.helio.i18n.resolver.CompositeLangResolver;
import cc.uncarbon.framework.helio.i18n.resolver.CompositeTimezoneResolver;
import cc.uncarbon.framework.helio.i18n.resolver.LangResolver;
import cc.uncarbon.framework.helio.i18n.resolver.TimezoneResolver;
import cc.uncarbon.framework.helio.i18n.resolver.lang.DefaultCompositeLangResolver;
import cc.uncarbon.framework.helio.i18n.resolver.lang.HeaderLangResolver;
import cc.uncarbon.framework.helio.i18n.resolver.lang.QueryParamLangResolver;
import cc.uncarbon.framework.helio.i18n.resolver.timezone.DefaultCompositeTimezoneResolver;
import cc.uncarbon.framework.helio.i18n.resolver.timezone.HeaderTimezoneResolver;
import cc.uncarbon.framework.helio.i18n.resolver.timezone.QueryParamTimezoneResolver;
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
 * Helio 集成国际化自动配置类
 *
 * @author Uncarbon
 */
@Conditional(value = HelioI18nAutoConfiguration.OnI18nEnabled.class)
@EnableConfigurationProperties(HelioI18nProperties.class)
@AutoConfiguration
public class HelioI18nAutoConfiguration {

    // -- 多语言

    @Bean(name = HelioI18nConstant.MESSAGE_SOURCE_SPRING_BEAN_NAME)
    public MessageSource messageSource(HelioI18nProperties props) {
        return new YamlMessageSource(props.getLang().getYamlBasenames(), StandardCharsets.UTF_8);
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryParamLangResolver queryParamLangResolver(HelioI18nProperties props) {
        return new QueryParamLangResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public HeaderLangResolver headerLangResolver(HelioI18nProperties props) {
        return new HeaderLangResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public CompositeLangResolver compositeLangResolver(HelioI18nProperties props, List<LangResolver> resolvers) {
        return new DefaultCompositeLangResolver(props, resolvers);
    }

    // -- 多时区

    @Bean
    @ConditionalOnMissingBean
    public QueryParamTimezoneResolver queryParamTimezoneResolver(HelioI18nProperties props) {
        return new QueryParamTimezoneResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public HeaderTimezoneResolver headerTimezoneResolver(HelioI18nProperties props) {
        return new HeaderTimezoneResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public CompositeTimezoneResolver compositeTimezoneResolver(HelioI18nProperties props, List<TimezoneResolver> resolvers) {
        return new DefaultCompositeTimezoneResolver(props, resolvers);
    }

    protected static class OnI18nEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = Objects.requireNonNull(context.getBeanFactory()).getBean(HelioI18nProperties.class);
            return Boolean.TRUE.equals(props.getEnabled());
        }
    }
}
