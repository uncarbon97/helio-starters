package cc.uncarbon.framework.helium.websecurity.autoconfigure;

import cc.uncarbon.framework.helium.base.constant.ServletFilterOrder;
import cc.uncarbon.framework.helium.websecurity.props.HeliumWebSecurityProperties;
import cc.uncarbon.framework.helium.websecurity.xss.XssFilter;
import jakarta.servlet.DispatcherType;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

/**
 * Helium Web 网络安全自动配置类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = HeliumWebSecurityProperties.class)
@AutoConfiguration
public class HeliumWebSecurityAutoConfiguration {

    /**
     * 反 XSS 注入
     */
    @Bean
    @Conditional(value = OnAntiXssEnabled.class)
    @ConditionalOnMissingBean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration(HeliumWebSecurityProperties props) {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter(props.getAntiXss().getIgnoredPaths()));
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(ServletFilterOrder.XSS_FILTER);
        return registration;
    }

    private static class OnAntiXssEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = Objects.requireNonNull(context.getBeanFactory()).getBean(HeliumWebSecurityProperties.class);
            var subProp = props.getAntiXss();
            return Boolean.TRUE.equals(subProp.getEnabled());
        }
    }
}
