package cc.uncarbon.framework.web.config;

import cc.uncarbon.framework.web.xss.XssFilter;
import javax.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 反XSS注入
 *
 * @author Uncarbon
 */
@Configuration
public class XssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<?> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(Integer.MAX_VALUE);
        return registration;
    }
}
