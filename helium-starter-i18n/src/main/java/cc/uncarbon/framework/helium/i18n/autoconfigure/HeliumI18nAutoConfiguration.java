package cc.uncarbon.framework.helium.i18n.autoconfigure;

import cc.uncarbon.framework.helium.base.condition.HeliumConditions;
import cc.uncarbon.framework.helium.base.errorcode.ErrorMessageFormatter;
import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.message.I18nAwareErrorMessageFormatter;
import cc.uncarbon.framework.helium.i18n.message.YamlMessageSource;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cc.uncarbon.framework.helium.i18n.resolver.currency.*;
import cc.uncarbon.framework.helium.i18n.resolver.lang.*;
import cc.uncarbon.framework.helium.i18n.resolver.timezone.*;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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

/**
 * Helium 集成国际化自动装配类
 * 先于 web 自动装配，优先注册 {@link I18nAwareErrorMessageFormatter}
 *
 * @author Uncarbon
 */
@Conditional(value = HeliumI18nAutoConfiguration.OnI18nEnabled.class)
@EnableConfigurationProperties(value = HeliumI18nProperties.class)
@AutoConfigureBefore(name = "cc.uncarbon.framework.helium.web.autoconfigure.HeliumWebAutoConfiguration")
@AutoConfiguration
public class HeliumI18nAutoConfiguration {

    // -- 错误消息格式化

    @Bean
    public ErrorMessageFormatter errorMessageFormatter() {
        return new I18nAwareErrorMessageFormatter();
    }

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

    // -- 多币种

    @Bean
    @ConditionalOnMissingBean
    public QueryParamCurrencyResolver queryParamCurrencyResolver(HeliumI18nProperties props) {
        return new QueryParamCurrencyResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public HeaderCurrencyResolver headerCurrencyResolver(HeliumI18nProperties props) {
        return new HeaderCurrencyResolver(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public CompositeCurrencyResolver compositeCurrencyResolver(HeliumI18nProperties props, List<CurrencyResolver> resolvers) {
        return new DefaultCompositeCurrencyResolver(props, resolvers);
    }

    static class OnI18nEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = HeliumConditions.bind(context.getEnvironment(), HeliumI18nProperties.class);
            return Boolean.TRUE.equals(props.getEnabled());
        }
    }
}
