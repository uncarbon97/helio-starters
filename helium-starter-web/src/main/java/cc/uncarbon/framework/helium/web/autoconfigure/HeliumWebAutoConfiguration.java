package cc.uncarbon.framework.helium.web.autoconfigure;

import cc.uncarbon.framework.helium.web.configurer.EnhanceWebMvcConfigurer;
import cc.uncarbon.framework.helium.web.handler.GlobalWebExceptionHandler;
import cc.uncarbon.framework.helium.web.listener.WebServerLaunchedListener;
import cc.uncarbon.framework.helium.web.props.HeliumWebProperties;
import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import jakarta.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Helium 增强 Web 自动装配类
 *
 * @author Uncarbon
 */
@Import(value = {EnhanceWebMvcConfigurer.class, GlobalWebExceptionHandler.class, WebServerLaunchedListener.class})
@EnableConfigurationProperties(value = HeliumWebProperties.class)
@AutoConfiguration
public class HeliumWebAutoConfiguration {

    /**
     * Validator 失败立即返回模式配置
     * 默认情况下会校验完所有字段，然后才抛出异常，所以设置为快速失败
     *
     * @author Charles7c@continew
     */
    @Bean
    @ConditionalOnMissingBean
    public Validator validator(MessageSource messageSource) {
        try (LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean()) {
            // 国际化
            factory.setValidationMessageSource(messageSource);
            // 快速失败
            factory.getValidationPropertyMap()
                    .put(BaseHibernateValidatorConfiguration.FAIL_FAST, Boolean.TRUE.toString());
            factory.afterPropertiesSet();
            return factory.getValidator();
        }
    }
}
