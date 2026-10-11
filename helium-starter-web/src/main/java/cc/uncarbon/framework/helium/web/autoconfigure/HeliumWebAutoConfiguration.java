package cc.uncarbon.framework.helium.web.autoconfigure;

import cc.uncarbon.framework.helium.base.errorcode.ErrorMessageFormatter;
import cc.uncarbon.framework.helium.web.exception.GlobalWebExceptionHandler;
import cc.uncarbon.framework.helium.web.props.HeliumWebProperties;
import cc.uncarbon.framework.helium.web.support.DefaultErrorMessageFormatter;
import cc.uncarbon.framework.helium.web.support.EnhanceWebMvcConfigurer;
import cc.uncarbon.framework.helium.web.support.WebServerLaunchedListener;
import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Helium 增强 Web 自动装配类
 *
 * @author Uncarbon
 */
@Import(value = {EnhanceWebMvcConfigurer.class, GlobalWebExceptionHandler.class, WebServerLaunchedListener.class})
@EnableConfigurationProperties(value = HeliumWebProperties.class)
@AutoConfigureBefore(value = ValidationAutoConfiguration.class)
@AutoConfiguration
public class HeliumWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = ErrorMessageFormatter.class)
    public ErrorMessageFormatter errorMessageFormatter() {
        return new DefaultErrorMessageFormatter();
    }

    /**
     * Validator 失败立即返回模式配置
     * 默认情况下会校验完所有字段，然后才抛出异常，所以设置为快速失败
     * <p>返回 {@link LocalValidatorFactoryBean} 本身（同时实现 Jakarta 与 Spring 两类 Validator）：
     * MVC 数据绑定与 @Valid 校验共用此实例，校验消息走 MessageSource 国际化；
     * 且 Bean 生命周期交容器管理，不可提前关闭内部 factory</p>
     *
     * @author Charles7c@continew
     */
    @Bean
    @ConditionalOnMissingBean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        // 国际化
        factory.setValidationMessageSource(messageSource);
        // 快速失败
        factory.getValidationPropertyMap()
                .put(BaseHibernateValidatorConfiguration.FAIL_FAST, Boolean.TRUE.toString());
        return factory;
    }
}
